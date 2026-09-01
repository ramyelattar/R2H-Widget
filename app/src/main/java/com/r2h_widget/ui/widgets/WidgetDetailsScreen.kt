package com.r2h_widget.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.r2h_widget.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.r2h_widget.catalog.HomeWidgetAvailability
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.ui.AnalogStudioSection
import com.r2h_widget.ui.WidgetDetailsAction
import com.r2h_widget.ui.WidgetDetailsUiState
import com.r2h_widget.ui.WidgetStudioMoreSection
import com.r2h_widget.ui.WidgetStudioSection
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.ui.theme.VoidBlack
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.battery.BatteryStateReader
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockPresetCatalog
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.pinning.WidgetPinningResult
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration

/**
 * The studio behind every catalog product: a live preview above, the product's
 * editor sections below, and one consistent Add / Apply action.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WidgetDetailsScreen(
    state: WidgetDetailsUiState,
    onBack: () -> Unit,
    onAction: (WidgetDetailsAction) -> Unit,
    onAddWidget: (String, DigitalClockConfiguration) -> WidgetPinningResult,
    onSaveWidget: (Int, String, DigitalClockConfiguration) -> WidgetPinningResult = { _, _, _ ->
        WidgetPinningResult.Unsupported
    },
    onAddAnalogWidget: (String, AnalogClockConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveAnalogWidget: (Int, String, AnalogClockConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddBatteryWidget: (String, BatteryWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveBatteryWidget: (Int, String, BatteryWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddWeatherWidget: (String, WeatherWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveWeatherWidget: (Int, String, WeatherWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddSearchWidget: (String, SearchWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveSearchWidget: (Int, String, SearchWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddMusicWidget: (String, MusicWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveMusicWidget: (Int, String, MusicWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
) {
    val definition = state.definition
    var menuExpanded by remember { mutableStateOf(false) }
    var savePresetDialogVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var pendingWeatherAdd by remember {
        mutableStateOf<Pair<String, WeatherWidgetConfiguration>?>(null)
    }
    var showBackgroundLocationEducation by remember {
        mutableStateOf(false)
    }

    fun hasForegroundLocationPermission(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun hasBackgroundLocationPermission(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun completePendingWeatherPin() {
        val pending = pendingWeatherAdd ?: return
        pendingWeatherAdd = null

        val result = onAddWeatherWidget(
            pending.first,
            pending.second,
        )

        onAction(WidgetDetailsAction.PinningCompleted(result))
    }

    fun failPendingWeatherPin(message: String) {
        pendingWeatherAdd = null
        onAction(
            WidgetDetailsAction.PinningCompleted(
                WidgetPinningResult.Failed(message),
            ),
        )
    }

    val backgroundLocationSettingsLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
        ) {
            if (hasBackgroundLocationPermission()) {
                completePendingWeatherPin()
            } else {
                failPendingWeatherPin(
                    "Background location is required for automatic weather updates when the app is closed.",
                )
            }
        }

    val foregroundLocationLauncher =
        androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val foregroundGranted =
                permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (!foregroundGranted) {
                failPendingWeatherPin(
                    "Location permission is required to use the Weather widget.",
                )
            } else if (hasBackgroundLocationPermission()) {
                completePendingWeatherPin()
            } else {
                showBackgroundLocationEducation = true
            }
        }

    fun beginWeatherPin(
        productId: String,
        configuration: WeatherWidgetConfiguration,
    ) {
        pendingWeatherAdd = productId to configuration

        when {
            !hasForegroundLocationPermission() -> {
                foregroundLocationLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }

            hasBackgroundLocationPermission() -> {
                completePendingWeatherPin()
            }

            else -> {
                showBackgroundLocationEducation = true
            }
        }
    }

    if (showBackgroundLocationEducation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showBackgroundLocationEducation = false
                failPendingWeatherPin(
                    "Background location was not enabled.",
                )
            },
            title = {
                Text("Allow background location")
            },
            text = {
                Text(
                    "To keep the Weather widget updated as you move, " +
                        "open Permissions > Location and choose “Allow all the time”.",
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showBackgroundLocationEducation = false

                        backgroundLocationSettingsLauncher.launch(
                            android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.parse("package:${context.packageName}"),
                            ),
                        )
                    },
                ) {
                    Text("Open settings")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showBackgroundLocationEducation = false
                        failPendingWeatherPin(
                            "Background location was not enabled.",
                        )
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(R.drawable.app_page_background),
                contentScale = ContentScale.Crop,
            ),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = GlassSurface.copy(alpha = 0.90f),
                ),
                title = { Text(definition?.displayName ?: "Widget Studio") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.semantics { contentDescription = "Back" },
                    ) { Text("‹", color = TextPrimary, style = MaterialTheme.typography.headlineMedium) }
                },
                actions = {
                    if (definition != null) {
                        FavoriteButton(
                            isFavorite = state.isFavorite,
                            onToggle = { onAction(WidgetDetailsAction.ToggleFavorite) },
                        )
                        if (definition.homeWidgetAvailability == HomeWidgetAvailability.DIGITAL_CLOCK) {
                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.semantics { contentDescription = "More options" },
                                ) { Text("⋮", color = TextPrimary, style = MaterialTheme.typography.titleLarge) }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Save current style") },
                                        onClick = {
                                            menuExpanded = false
                                            savePresetDialogVisible = true
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (definition != null) {
                Surface(color = GlassSurface.copy(alpha = 0.90f), tonalElevation = 3.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        when (val result = state.pinningResult) {
                            WidgetPinningResult.Requested -> Text(
                                "Pin request sent. Finish adding it from your launcher.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            WidgetPinningResult.Unsupported -> Text(
                                "Direct pinning is unavailable here. Add this widget from your launcher's widget picker.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            is WidgetPinningResult.Failed -> Text(result.message, color = TextSecondary)
                            null -> Unit
                        }
                        val isEditingExisting = state.appWidgetId != null
                        Button(
                            onClick = {
                                if (
                                    definition.homeWidgetAvailability == HomeWidgetAvailability.WEATHER &&
                                    state.appWidgetId == null
                                ) {
                                    beginWeatherPin(
                                        definition.id,
                                        state.weatherConfiguration,
                                    )
                                } else {
                                    val result = when (definition.homeWidgetAvailability) {
                                    HomeWidgetAvailability.DIGITAL_CLOCK -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveWidget(appWidgetId, definition.id, state.configuration)
                                    } ?: onAddWidget(definition.id, state.configuration)

                                    HomeWidgetAvailability.ANALOG_CLOCK -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveAnalogWidget(appWidgetId, definition.id, state.analogConfiguration)
                                    } ?: onAddAnalogWidget(definition.id, state.analogConfiguration)

                                    HomeWidgetAvailability.BATTERY -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveBatteryWidget(appWidgetId, definition.id, state.batteryConfiguration)
                                    } ?: onAddBatteryWidget(definition.id, state.batteryConfiguration)

                                    HomeWidgetAvailability.WEATHER -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveWeatherWidget(appWidgetId, definition.id, state.weatherConfiguration)
                                    } ?: onAddWeatherWidget(definition.id, state.weatherConfiguration)

                                    HomeWidgetAvailability.SEARCH -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveSearchWidget(appWidgetId, definition.id, state.searchConfiguration)
                                    } ?: onAddSearchWidget(definition.id, state.searchConfiguration)

                                    HomeWidgetAvailability.MUSIC -> state.appWidgetId?.let { appWidgetId ->
                                        onSaveMusicWidget(
                                            appWidgetId,
                                            definition.id,
                                            state.musicConfiguration,
                                        )
                                    } ?: onAddMusicWidget(
                                        definition.id,
                                        state.musicConfiguration,
                                    )

                                    HomeWidgetAvailability.NONE -> WidgetPinningResult.Unsupported
                                }
                                    onAction(WidgetDetailsAction.PinningCompleted(result))
                                }
                            },
                            enabled = definition.homeWidgetAvailability != HomeWidgetAvailability.NONE,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        ) { Text(if (isEditingExisting) "Apply Changes" else "Add Widget") }
                    }
                }
            }
        },
    ) { innerPadding ->
        if (definition == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(state.errorMessage ?: "Widget unavailable", color = TextPrimary)
                Button(onClick = onBack) { Text("Back to widgets") }
            }
            return@Scaffold
        }

        val listState = rememberLazyListState()
        val showCompactPreview by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 180
            }
        }
        val setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit) = { transform ->
            onAction(WidgetDetailsAction.SetConfiguration(transform(state.configuration)))
        }
        val setAnalogConfiguration: ((AnalogClockConfiguration) -> Unit) = { value ->
            onAction(WidgetDetailsAction.SetAnalogConfiguration(value))
        }
        val realBatteryState = remember {
            runCatching { BatteryStateReader.read(context) }.getOrNull()
        }
        val recentColors = state.recentColors.ifEmpty {
            listOf(
                definition.previewAccentHex,
                state.batteryConfiguration.progressColorHex,
                state.weatherConfiguration.temperatureColorHex,
            ).distinct()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = AppSpacing.lg,
                    end = AppSpacing.lg,
                    top = AppSpacing.md,
                    bottom = AppSpacing.xxl,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                item(key = "full-preview") {
                    StudioPreview(
                        definition = definition,
                        state = state,
                        batteryState = realBatteryState,
                        compact = false,
                    )
                }
                if (definition.homeWidgetAvailability in listOf(
                        HomeWidgetAvailability.DIGITAL_CLOCK,
                        HomeWidgetAvailability.ANALOG_CLOCK,
                    )
                ) {
                    item(key = "size-selector") {
                        SizeSelector(state.previewSizeClass, onAction)
                    }
                }
                item(key = "studio-heading") {
                    Text(
                        "Customize your ${definition.displayName.lowercase()}",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                item(key = "editor") {
                    when (definition.homeWidgetAvailability) {
                        HomeWidgetAvailability.DIGITAL_CLOCK -> DigitalClockStudio(
                            state = state,
                            onAction = onAction,
                            setConfiguration = setConfiguration,
                            recentColors = recentColors,
                            onRequestSave = { savePresetDialogVisible = true },
                        )

                        HomeWidgetAvailability.ANALOG_CLOCK -> Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        ) {
                            AnalogClockSectionTabs(
                                selected = state.selectedAnalogSection,
                                onSelected = { section ->
                                    onAction(WidgetDetailsAction.SelectAnalogSection(section))
                                },
                            )
                            AnalogClockEditor(
                                section = state.selectedAnalogSection,
                                configuration = state.analogConfiguration,
                                recentColors = recentColors,
                                setConfiguration = setAnalogConfiguration,
                            )
                        }

                        HomeWidgetAvailability.BATTERY -> BatteryWidgetEditor(
                            configuration = state.batteryConfiguration,
                            recentColors = recentColors,
                            setConfiguration = { value ->
                                onAction(WidgetDetailsAction.SetBatteryConfiguration(value))
                            },
                        )

                        HomeWidgetAvailability.WEATHER -> WeatherWidgetEditor(
                            configuration = state.weatherConfiguration,
                            recentColors = recentColors,
                            setConfiguration = { value ->
                                onAction(WidgetDetailsAction.SetWeatherConfiguration(value))
                            },
                        )

                        HomeWidgetAvailability.SEARCH -> SearchWidgetEditor(
                            configuration = state.searchConfiguration,
                            recentColors = recentColors,
                            setConfiguration = { value ->
                                onAction(WidgetDetailsAction.SetSearchConfiguration(value))
                            },
                        )

                        HomeWidgetAvailability.MUSIC -> MusicWidgetEditor(
                            configuration = state.musicConfiguration,
                            setConfiguration = { value ->
                                onAction(
                                    WidgetDetailsAction.SetMusicConfiguration(
                                        value,
                                    ),
                                )
                            },
                        )

                        HomeWidgetAvailability.NONE -> Unit
                    }
                }
            }
            AnimatedVisibility(
                visible = showCompactPreview,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                    .animateContentSize(),
            ) {
                StudioPreview(
                    definition = definition,
                    state = state,
                    batteryState = realBatteryState,
                    compact = true,
                )
            }
        }
    }

    if (savePresetDialogVisible) {
        SavePresetDialog(
            onDismiss = { savePresetDialogVisible = false },
            onSave = { name ->
                savePresetDialogVisible = false
                onAction(WidgetDetailsAction.SaveCustomPreset(name))
            },
        )
    }
}

@Composable
private fun DigitalClockStudio(
    state: WidgetDetailsUiState,
    onAction: (WidgetDetailsAction) -> Unit,
    setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit),
    recentColors: List<String>,
    onRequestSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        PrimarySectionTabs(state.selectedSection, onAction)
        if (state.selectedSection == WidgetStudioSection.MORE) {
            MoreSectionTabs(state.selectedMoreSection, onAction)
        }
        when (state.selectedSection) {
            WidgetStudioSection.STYLES -> PresetsEditor(
                state = state,
                onAction = onAction,
                onRequestSave = onRequestSave,
            )
            WidgetStudioSection.TIME -> TimeEditor(state.configuration, recentColors, setConfiguration)
            WidgetStudioSection.DATE -> DateEditor(state.configuration, recentColors, setConfiguration)
            WidgetStudioSection.LAYOUT -> LayoutEditor(state.configuration, setConfiguration)
            WidgetStudioSection.MORE -> when (state.selectedMoreSection) {
                WidgetStudioMoreSection.BACKGROUND -> BackgroundEditorSection(
                    background = state.configuration.background,
                    effects = state.configuration.effects,
                    recentColors = recentColors,
                    showEffects = true,
                    setBackground = { value ->
                        onAction(
                            WidgetDetailsAction.SetConfiguration(
                                state.configuration.copy(background = value),
                            ),
                        )
                    },
                    setEffects = { value ->
                        onAction(
                            WidgetDetailsAction.SetConfiguration(
                                state.configuration.copy(effects = value),
                            ),
                        )
                    },
                )
                WidgetStudioMoreSection.EFFECTS -> EditorCard("Effects", "A little atmosphere goes a long way.") {
                    ToggleControl("Glow", state.configuration.effects.glowEnabled) { value ->
                        setConfiguration { it.copy(effects = it.effects.copy(glowEnabled = value)) }
                    }
                    if (state.configuration.effects.glowEnabled) {
                        ColorProperty("Glow color", state.configuration.effects.glowColorHex, recentColors) { value ->
                            setConfiguration { it.copy(effects = it.effects.copy(glowColorHex = value)) }
                        }
                        SliderControl("Strength", state.configuration.effects.glowStrength, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
                            setConfiguration { it.copy(effects = it.effects.copy(glowStrength = value)) }
                        }
                    }
                }
                WidgetStudioMoreSection.ACTIONS -> ActionsEditorSection(
                    action = state.configuration.action,
                    setAction = { value ->
                        setConfiguration { it.copy(action = value) }
                    },
                )
                WidgetStudioMoreSection.ADVANCED -> AdvancedEditor(state.configuration, setConfiguration)
            }
        }
    }
}

@Composable
private fun StudioPreview(
    definition: WidgetDefinition,
    state: WidgetDetailsUiState,
    batteryState: com.r2h_widget.widget.battery.BatteryWidgetState?,
    compact: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 78.dp else 210.dp)
            .clip(RoundedCornerShape(if (compact) AppCorners.button else AppCorners.card))
            .border(1.dp, GlassOutline, RoundedCornerShape(if (compact) AppCorners.button else AppCorners.card))
            .background(GlassSurface),
    ) {
        WidgetPreviewRenderer(
            definition = definition,
            configuration = state.configuration,
            analogConfiguration = state.analogConfiguration,
            batteryConfiguration = state.batteryConfiguration,
            batteryState = batteryState,
            weatherConfiguration = state.weatherConfiguration,
            searchConfiguration = state.searchConfiguration,
            musicConfiguration = state.musicConfiguration,
            sizeClass = state.previewSizeClass,
        )
    }
}

@Composable
private fun SizeSelector(
    selected: WidgetSizeClass,
    onAction: (WidgetDetailsAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.button))
            .background(GlassSurface)
            .padding(AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        listOf(
            "2×1" to WidgetSizeClass.COMPACT,
            "2×2" to WidgetSizeClass.MEDIUM,
            "4×2" to WidgetSizeClass.EXPANDED,
        ).forEach { (label, sizeClass) ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(AppCorners.button))
                    .clickable { onAction(WidgetDetailsAction.SetPreviewSizeClass(sizeClass)) }
                    .semantics {
                        contentDescription = "Preview size $label"
                        role = Role.Button
                    },
                color = if (selected == sizeClass) AccentViolet.copy(alpha = 0.22f) else androidx.compose.ui.graphics.Color.Transparent,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(label, color = if (selected == sizeClass) TextPrimary else TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun PrimarySectionTabs(selected: WidgetStudioSection, onAction: (WidgetDetailsAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppCorners.button))
            .background(GlassSurface)
            .padding(AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        WidgetStudioSection.entries.forEach { section ->
            StudioTab(
                label = section.label,
                selected = selected == section,
                modifier = Modifier.weight(1f),
                onClick = { onAction(WidgetDetailsAction.SelectSection(section)) },
            )
        }
    }
}

@Composable
private fun MoreSectionTabs(selected: WidgetStudioMoreSection, onAction: (WidgetDetailsAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        WidgetStudioMoreSection.entries.forEach { section ->
            FilterChip(
                selected = selected == section,
                onClick = { onAction(WidgetDetailsAction.SelectMoreSection(section)) },
                label = { Text(section.label, maxLines = 1) },
            )
        }
    }
}

@Composable
internal fun PresetsEditor(
    state: WidgetDetailsUiState,
    onAction: (WidgetDetailsAction) -> Unit,
    onRequestSave: () -> Unit,
) {
    EditorCard("Styles", "Start with a mood, then make it yours.") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ClockPresetCatalog.builtIn.forEach { preset ->
                PresetPreviewCard(
                    name = preset.name,
                    selected = state.configuration.presetId == preset.id,
                    configuration = preset.configuration,
                    onClick = { onAction(WidgetDetailsAction.ApplyPreset(preset.id)) },
                )
            }
        }
        if (state.customPresets.isNotEmpty()) {
            Text("Saved styles", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            state.customPresets.forEach { preset ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onAction(WidgetDetailsAction.ApplyPreset(preset.id)) },
                        modifier = Modifier.weight(1f),
                    ) { Text(preset.name, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Start) }
                    TextButton(onClick = { onAction(WidgetDetailsAction.DeleteCustomPreset(preset.id)) }) {
                        Text("Delete", color = TextSecondary)
                    }
                }
            }
        }
        OutlinedButton(onClick = onRequestSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save current style")
        }
    }
}

@Composable
private fun PresetPreviewCard(
    name: String,
    selected: Boolean,
    configuration: DigitalClockConfiguration,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(AppCorners.button))
            .background(if (selected) AccentViolet.copy(alpha = 0.14f) else com.r2h_widget.ui.theme.GlassSurfaceStrong)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AccentViolet else GlassOutline,
                shape = RoundedCornerShape(AppCorners.button),
            )
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                contentDescription = "Preset $name"
                role = Role.Button
            }
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(AppCorners.button)),
        ) {
            WidgetPreviewRenderer(
                definition = com.r2h_widget.catalog.WidgetCatalog.digitalClock,
                configuration = configuration,
                sizeClass = WidgetSizeClass.MEDIUM,
                live = false,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = TextPrimary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            if (selected) Text("✓", color = AccentViolet)
        }
    }
}

@Composable
internal fun FontProperty(label: String, selected: ClockFontFamily, onSelected: (ClockFontFamily) -> Unit) {
    var sheetVisible by remember { mutableStateOf(false) }
    StudioPropertyRow(
        label = label,
        value = selected.label,
        onClick = { sheetVisible = true },
        sample = {
            Text("Digital", color = TextSecondary, fontFamily = selected.toComposeFontFamily(), style = MaterialTheme.typography.labelSmall)
        },
    )
    if (sheetVisible) {
        FontPickerSheet(
            initialValue = selected,
            onDismiss = { sheetVisible = false },
            onApply = onSelected,
        )
    }
}

@Composable
internal fun DateFormatProperty(configuration: DigitalClockConfiguration, onSelected: (ClockDateFormatPreset) -> Unit) {
    var sheetVisible by remember { mutableStateOf(false) }
    StudioPropertyRow("Date format", configuration.date.formatPreset.label, onClick = { sheetVisible = true })
    if (sheetVisible) {
        DateFormatPickerSheet(
            configuration = configuration,
            initialValue = configuration.date.formatPreset,
            onDismiss = { sheetVisible = false },
            onApply = onSelected,
        )
    }
}

@Composable
private fun SavePresetDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = com.r2h_widget.ui.theme.GlassSurfaceStrong,
        title = { Text("Save current style", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(32) },
                label = { Text("Preset name") },
                singleLine = true,
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            Button(onClick = { onSave(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
    )
}













