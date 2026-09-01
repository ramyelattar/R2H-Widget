package com.r2h_widget.customization

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CustomizationUiState(
    val selections: CustomizationSelections = CustomizationSelections(),
    val iconPacks: List<IconPackDefinition> = IconPackCatalog.builtIn,
    val iconPackBundle: IconPackBundleStatus = IconPackBundleStatus.empty(),
    val discoveredApps: List<DiscoveredAppIcon> = emptyList(),
    val iconCoverage: Map<String, IconPackCoverage> = emptyMap(),
    val wallpaperCategory: WallpaperCategory = WallpaperCategory.ALL,
    val wallpaperQuery: String = "",
    val iconQuery: String = "",
    val isLoadingIcons: Boolean = true,
    val message: String? = null,
)

sealed interface CustomizationAction {
    data class SelectIconPack(val id: String) : CustomizationAction
    data class ToggleIconPackFavorite(val id: String) : CustomizationAction
    data object InstallIconPacks : CustomizationAction
    data object RefreshIconPackInstallation : CustomizationAction
    data object OpenIconPackSettings : CustomizationAction
    data object ApplySelectedIconPack : CustomizationAction
    data class SetIconQuery(val query: String) : CustomizationAction
    data class SelectWallpaperCategory(val category: WallpaperCategory) : CustomizationAction
    data class SetWallpaperQuery(val query: String) : CustomizationAction
    data class SelectWallpaper(val id: String) : CustomizationAction
    data class ToggleWallpaperFavorite(val id: String) : CustomizationAction
    data class ApplyWallpaper(val target: WallpaperTarget) : CustomizationAction
    data class ApplyTheme(val id: String) : CustomizationAction
    data object ClearMessage : CustomizationAction
}

class CustomizationViewModel(
    context: Context,
    private val repository: CustomizationRepository = InMemoryCustomizationRepository(),
) : ViewModel() {
    private val appContext = context.applicationContext
    private val engine = IconPackEngine(appContext)
    private val iconPackInstaller = IconPackBundleInstaller(appContext)
    private val _uiState = MutableStateFlow(CustomizationUiState())
    val uiState: StateFlow<CustomizationUiState> = _uiState.asStateFlow()
    private var discoveredSource: List<DiscoveredAppIcon> = emptyList()

    init {
        viewModelScope.launch {
            iconPackInstaller.state.collect { status ->
                _uiState.update { it.copy(iconPackBundle = status) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) { iconPackInstaller.refresh() }
        viewModelScope.launch {
            repository.selections.collect { selections ->
                _uiState.update { it.copy(selections = selections) }
                refreshRenderedIcons(selections)
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            val apps = runCatching { engine.discoverLaunchableApps() }.getOrDefault(emptyList())
            discoveredSource = apps
            val coverage = IconPackCatalog.builtIn.associate { pack -> pack.id to engine.coverage(pack, apps) }
            val selectedPack = IconPackCatalog.findById(_uiState.value.selections.iconPackId)
            val rendered = if (selectedPack == null) apps else apps.map { app ->
                app.copy(bitmap = engine.renderIcon(app, selectedPack))
            }
            _uiState.update {
                it.copy(
                    discoveredApps = rendered,
                    iconCoverage = coverage,
                    isLoadingIcons = false,
                )
            }
        }
    }

    fun onAction(action: CustomizationAction) {
        when (action) {
            is CustomizationAction.SelectIconPack -> viewModelScope.launch { repository.selectIconPack(action.id) }
            is CustomizationAction.ToggleIconPackFavorite -> viewModelScope.launch {
                repository.toggleIconPackFavorite(action.id)
            }
            CustomizationAction.InstallIconPacks -> viewModelScope.launch(Dispatchers.IO) {
                when (val result = iconPackInstaller.installOrUpdate()) {
                    IconPackInstallStartResult.Started -> showMessage("Installing the four launcher icon packs…")
                    IconPackInstallStartResult.PermissionRequired -> {
                        runCatching {
                            appContext.startActivity(
                                iconPackInstaller.unknownSourcesIntent().addFlags(
                                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK,
                                ),
                            )
                        }
                        showMessage(
                            "Allow installs from R2H in Android settings, then tap Install Icon Packs again.",
                        )
                    }
                    IconPackInstallStartResult.AlreadyInstalled -> showMessage("All four icon packs are already installed.")
                    is IconPackInstallStartResult.Failed -> showMessage(result.message)
                }
            }
            CustomizationAction.RefreshIconPackInstallation -> viewModelScope.launch(Dispatchers.IO) {
                iconPackInstaller.refresh()
            }
            CustomizationAction.OpenIconPackSettings -> {
                val intent = iconPackInstaller.launcherPickerIntent()
                if (intent != null) {
                    runCatching { appContext.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        .onFailure { showMessage("Open Home > Customise > Icon Pack to choose an installed pack.") }
                } else {
                    showMessage("Open Home > Customise > Icon Pack to choose an installed pack.")
                }
            }
            CustomizationAction.ApplySelectedIconPack -> {
                val pack = IconPackCatalog.findById(_uiState.value.selections.iconPackId) ?: return
                val result = engine.launcherApplyResult(pack)
                _uiState.update {
                    it.copy(message = when (result) {
                        is IconApplicationResult.ManualSelectionRequired -> result.message
                        is IconApplicationResult.Unsupported -> result.message
                    })
                }
            }
            is CustomizationAction.SetIconQuery -> _uiState.update { it.copy(iconQuery = action.query) }
            is CustomizationAction.SelectWallpaperCategory -> _uiState.update {
                it.copy(wallpaperCategory = action.category)
            }
            is CustomizationAction.SetWallpaperQuery -> _uiState.update { it.copy(wallpaperQuery = action.query) }
            is CustomizationAction.SelectWallpaper -> viewModelScope.launch { repository.selectWallpaper(action.id) }
            is CustomizationAction.ToggleWallpaperFavorite -> viewModelScope.launch {
                repository.toggleWallpaperFavorite(action.id)
            }
            is CustomizationAction.ApplyWallpaper -> applyWallpaper(action.target)
            is CustomizationAction.ApplyTheme -> viewModelScope.launch {
                val theme = ThemeCatalog.findById(action.id) ?: return@launch
                repository.applyTheme(theme.id)
                val wallpaperResult = applyWallpaperFor(
                    wallpaperId = theme.wallpaperId,
                    target = WallpaperTarget.BOTH,
                )
                _uiState.update {
                    it.copy(
                        message = when (wallpaperResult) {
                            WallpaperApplyResult.Applied ->
                                "${theme.name} applied. Choose its icon pack in your launcher's icon settings."

                            is WallpaperApplyResult.Failed ->
                                "${theme.name} saved, but its wallpaper could not be applied: ${wallpaperResult.message} " +
                                    "Choose its icon pack in your launcher's icon settings."
                        },
                    )
                }
            }
            CustomizationAction.ClearMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun applyWallpaper(target: WallpaperTarget) {
        val wallpaperId = _uiState.value.selections.wallpaperId
        viewModelScope.launch(Dispatchers.IO) {
            val result = applyWallpaperFor(wallpaperId, target)
            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        message = when (result) {
                            WallpaperApplyResult.Applied -> "Wallpaper applied to ${target.label.lowercase()} screen."
                            is WallpaperApplyResult.Failed -> result.message
                        },
                    )
                }
            }
        }
    }

    private suspend fun applyWallpaperFor(
        wallpaperId: String,
        target: WallpaperTarget,
    ): WallpaperApplyResult = withContext(Dispatchers.IO) {
        val wallpaper = WallpaperCatalog.findById(wallpaperId)
            ?: return@withContext WallpaperApplyResult.Failed("The selected wallpaper is no longer available.")
        runCatching {
            val bitmap = BitmapFactory.decodeResource(appContext.resources, wallpaper.drawableRes)
                ?: error("The selected wallpaper could not be decoded.")
            val flags = when (target) {
                WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }
            WallpaperManager.getInstance(appContext).setBitmap(bitmap, null, true, flags)
            WallpaperApplyResult.Applied
        }.getOrElse { error ->
            WallpaperApplyResult.Failed(
                error.message ?: "The wallpaper could not be applied on this device.",
            )
        }
    }

    private fun refreshRenderedIcons(selections: CustomizationSelections) {
        val pack = IconPackCatalog.findById(selections.iconPackId) ?: return
        if (discoveredSource.isEmpty()) return
        viewModelScope.launch(Dispatchers.Default) {
            val rendered = discoveredSource.map { app -> app.copy(bitmap = engine.renderIcon(app, pack)) }
            _uiState.update { state ->
                if (state.selections.iconPackId == pack.id) state.copy(discoveredApps = rendered) else state
            }
        }
    }

    private fun showMessage(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _uiState.update { it.copy(message = message) }
        }
    }

    override fun onCleared() {
        iconPackInstaller.close()
        super.onCleared()
    }
}

private val WallpaperTarget.label: String
    get() = when (this) {
        WallpaperTarget.HOME -> "home"
        WallpaperTarget.LOCK -> "lock"
        WallpaperTarget.BOTH -> "home and lock"
    }
