package com.r2h_widget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.data.FavoritesRepository
import com.r2h_widget.data.InMemoryFavoritesRepository
import com.r2h_widget.data.InMemoryWidgetPresetRepository
import com.r2h_widget.data.WidgetPresetRepository
import com.r2h_widget.customization.CustomizationRepository
import com.r2h_widget.customization.InMemoryCustomizationRepository
import com.r2h_widget.data.InMemoryWidgetInstanceRepository
import com.r2h_widget.data.WidgetInstanceRepository
import com.r2h_widget.data.InMemoryTypedWidgetInstanceRepository
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstanceRepository
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.clock.ClockAccent
import com.r2h_widget.widget.clock.ClockPresetCatalog
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.clock.ClockWidgetPreset
import com.r2h_widget.widget.clock.defaultClockConfiguration
import com.r2h_widget.widget.pinning.WidgetPinningResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WidgetDetailsUiState(
    val widgetId: String? = null,
    val appWidgetId: Int? = null,
    val definition: WidgetDefinition? = null,
    val configuration: DigitalClockConfiguration = DigitalClockConfiguration(),
    val analogConfiguration: AnalogClockConfiguration = AnalogClockConfiguration(),
    val batteryConfiguration: BatteryWidgetConfiguration = BatteryWidgetConfiguration(),
    val weatherConfiguration: WeatherWidgetConfiguration = WeatherWidgetConfiguration(),
    val searchConfiguration: SearchWidgetConfiguration = SearchWidgetConfiguration(),
    val musicConfiguration: MusicWidgetConfiguration = MusicWidgetConfiguration(),
    val isFavorite: Boolean = false,
    val pinningResult: WidgetPinningResult? = null,
    val errorMessage: String? = null,
    val selectedSection: WidgetStudioSection = WidgetStudioSection.STYLES,
    val selectedMoreSection: WidgetStudioMoreSection = WidgetStudioMoreSection.BACKGROUND,
    val selectedAnalogSection: AnalogStudioSection = AnalogStudioSection.FACE,
    val previewSizeClass: WidgetSizeClass = WidgetSizeClass.MEDIUM,
    val recentColors: List<String> = emptyList(),
    val customPresets: List<ClockWidgetPreset> = emptyList(),
)

enum class WidgetStudioSection(val label: String) {
    STYLES("Styles"),
    TIME("Time"),
    DATE("Date"),
    LAYOUT("Layout"),
    MORE("More"),
}

enum class WidgetStudioMoreSection(val label: String) {
    BACKGROUND("Background"),
    EFFECTS("Effects"),
    ACTIONS("Actions"),
    ADVANCED("Advanced"),
}

/** Sections inside the analog clock studio. */
enum class AnalogStudioSection(val label: String) {
    FACE("Face"),
    HANDS("Hands"),
    DATE("Date"),
    LAYOUT("Layout"),
    MORE("More"),
}

sealed interface WidgetDetailsAction {
    data class Set24HourFormat(val enabled: Boolean) : WidgetDetailsAction
    data class SetShowDate(val enabled: Boolean) : WidgetDetailsAction
    data class SetShowWeekday(val enabled: Boolean) : WidgetDetailsAction
    data class SetAccent(val accent: ClockAccent) : WidgetDetailsAction
    data class SetConfiguration(val configuration: DigitalClockConfiguration) : WidgetDetailsAction
    data class SetAnalogConfiguration(val configuration: AnalogClockConfiguration) : WidgetDetailsAction
    data class SetBatteryConfiguration(val configuration: BatteryWidgetConfiguration) : WidgetDetailsAction
    data class SetWeatherConfiguration(val configuration: WeatherWidgetConfiguration) : WidgetDetailsAction
    data class SetSearchConfiguration(val configuration: SearchWidgetConfiguration) : WidgetDetailsAction
    data class SetMusicConfiguration(val configuration: MusicWidgetConfiguration) : WidgetDetailsAction
    data class ApplyPreset(val presetId: String) : WidgetDetailsAction
    data class SaveCustomPreset(val name: String) : WidgetDetailsAction
    data class DeleteCustomPreset(val presetId: String) : WidgetDetailsAction
    data class SelectSection(val section: WidgetStudioSection) : WidgetDetailsAction
    data class SelectMoreSection(val section: WidgetStudioMoreSection) : WidgetDetailsAction
    data class SelectAnalogSection(val section: AnalogStudioSection) : WidgetDetailsAction
    data class SetPreviewSizeClass(val sizeClass: WidgetSizeClass) : WidgetDetailsAction
    data object ToggleFavorite : WidgetDetailsAction
    data class PinningCompleted(val result: WidgetPinningResult) : WidgetDetailsAction
}

class WidgetDetailsViewModel(
    private val favoritesRepository: FavoritesRepository = InMemoryFavoritesRepository(),
    private val widgetInstanceRepository: WidgetInstanceRepository = InMemoryWidgetInstanceRepository(),
    private val typedWidgetInstanceRepository: TypedWidgetInstanceRepository = InMemoryTypedWidgetInstanceRepository(),
    private val presetRepository: WidgetPresetRepository = InMemoryWidgetPresetRepository(),
    private val customizationRepository: CustomizationRepository = InMemoryCustomizationRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(WidgetDetailsUiState())
    val uiState: StateFlow<WidgetDetailsUiState> = _uiState.asStateFlow()
    private var currentWidgetId: String? = null
    private var selectedClockPresetId: String? = null

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { favoriteIds ->
                _uiState.update { state ->
                    state.copy(isFavorite = state.widgetId?.let(favoriteIds::contains) == true)
                }
            }
        }
        viewModelScope.launch {
            presetRepository.customPresets.collect { presets ->
                _uiState.update { it.copy(customPresets = presets) }
            }
        }
        viewModelScope.launch {
            customizationRepository.selections.collect { selections ->
                selectedClockPresetId = selections.clockPresetId
            }
        }
    }

    fun openWidget(widgetId: String, appWidgetId: Int? = null) {
        val canonicalId = WidgetProductIds.canonicalize(widgetId)
        if (currentWidgetId == canonicalId && _uiState.value.widgetId == canonicalId && _uiState.value.appWidgetId == appWidgetId) return
        currentWidgetId = canonicalId
        val definition = WidgetCatalog.findById(canonicalId)
        _uiState.value = if (definition == null) {
            WidgetDetailsUiState(
                widgetId = canonicalId,
                appWidgetId = appWidgetId,
                errorMessage = "This widget is no longer available.",
            )
        } else {
            WidgetDetailsUiState(
                widgetId = canonicalId,
                appWidgetId = appWidgetId,
                definition = definition,
                configuration = initialClockConfiguration(definition),
                analogConfiguration = AnalogClockConfiguration(),
                batteryConfiguration = BatteryWidgetConfiguration(),
                weatherConfiguration = WeatherWidgetConfiguration(),
                searchConfiguration = SearchWidgetConfiguration(),
                musicConfiguration = MusicWidgetConfiguration(),
                previewSizeClass = defaultPreviewSizeClass(definition),
            )
        }
        if (definition != null && appWidgetId != null) {
            loadPersistedInstance(canonicalId, definition, appWidgetId)
        }
    }

    private fun defaultPreviewSizeClass(definition: WidgetDefinition) = when (definition.size) {
        com.r2h_widget.catalog.WidgetSize.TWO_BY_ONE -> WidgetSizeClass.COMPACT
        else -> WidgetSizeClass.MEDIUM
    }

    private fun initialClockConfiguration(definition: WidgetDefinition): DigitalClockConfiguration {
        if (definition.homeWidgetAvailability != com.r2h_widget.catalog.HomeWidgetAvailability.DIGITAL_CLOCK) {
            return definition.defaultClockConfiguration()
        }
        val presetId = selectedClockPresetId ?: return definition.defaultClockConfiguration()
        return ClockPresetCatalog.findById(presetId)?.configuration ?: definition.defaultClockConfiguration()
    }

    private fun loadPersistedInstance(canonicalId: String, definition: WidgetDefinition, appWidgetId: Int) {
        viewModelScope.launch {
            if (definition.homeWidgetAvailability == com.r2h_widget.catalog.HomeWidgetAvailability.DIGITAL_CLOCK) {
                val instance = widgetInstanceRepository.get(appWidgetId) ?: return@launch
                if (WidgetProductIds.canonicalize(instance.productId) != canonicalId) return@launch
                if (_uiState.value.widgetId == canonicalId && _uiState.value.appWidgetId == appWidgetId) {
                    _uiState.update { it.copy(configuration = instance.configuration) }
                }
                return@launch
            }
            val instance = typedWidgetInstanceRepository.get(appWidgetId) ?: return@launch
            if (WidgetProductIds.canonicalize(instance.productId) != canonicalId) return@launch
            if (_uiState.value.widgetId != canonicalId || _uiState.value.appWidgetId != appWidgetId) return@launch
            when (val configuration = instance.configuration) {
                is TypedWidgetConfiguration.Battery -> _uiState.update { it.copy(batteryConfiguration = configuration.value) }
                is TypedWidgetConfiguration.Weather -> _uiState.update { it.copy(weatherConfiguration = configuration.value) }
                is TypedWidgetConfiguration.Analog -> _uiState.update { it.copy(analogConfiguration = configuration.value) }
                is TypedWidgetConfiguration.Search -> _uiState.update { it.copy(searchConfiguration = configuration.value) }
                is TypedWidgetConfiguration.Music -> _uiState.update { it.copy(musicConfiguration = configuration.value) }
                is TypedWidgetConfiguration.Calendar -> Unit
            }
        }
    }

    fun onAction(action: WidgetDetailsAction) {
        when (action) {
            is WidgetDetailsAction.Set24HourFormat -> updateConfiguration {
                with24HourFormat(action.enabled)
            }

            is WidgetDetailsAction.SetShowDate -> updateConfiguration {
                withShowDate(action.enabled)
            }

            is WidgetDetailsAction.SetShowWeekday -> updateConfiguration {
                withShowWeekday(action.enabled)
            }

            is WidgetDetailsAction.SetAccent -> updateConfiguration {
                withAccent(action.accent)
            }

            is WidgetDetailsAction.SetConfiguration -> {
                val colors = listOf(
                    action.configuration.typography.timeColorHex,
                    action.configuration.typography.dateColorHex,
                    action.configuration.background.highlightColorHex,
                )
                _uiState.update { state ->
                    state.copy(
                        configuration = action.configuration.copy(presetId = null),
                        recentColors = (colors + state.recentColors).distinct().take(8),
                    )
                }
            }

            is WidgetDetailsAction.SetAnalogConfiguration -> _uiState.update {
                it.copy(analogConfiguration = action.configuration)
            }

            is WidgetDetailsAction.SetBatteryConfiguration -> _uiState.update {
                it.copy(batteryConfiguration = action.configuration)
            }

            is WidgetDetailsAction.SetWeatherConfiguration -> _uiState.update {
                it.copy(weatherConfiguration = action.configuration)
            }

            is WidgetDetailsAction.SetSearchConfiguration -> _uiState.update {
                it.copy(searchConfiguration = action.configuration)
            }

            is WidgetDetailsAction.SetMusicConfiguration -> _uiState.update {
                it.copy(musicConfiguration = action.configuration)
            }

            is WidgetDetailsAction.ApplyPreset -> {
                val preset = ClockPresetCatalog.findById(action.presetId)
                    ?: _uiState.value.customPresets.firstOrNull { it.id == action.presetId }
                    ?: return
                val colors = listOf(
                    preset.configuration.typography.timeColorHex,
                    preset.configuration.typography.dateColorHex,
                    preset.configuration.background.highlightColorHex,
                )
                _uiState.update { state ->
                    state.copy(
                        configuration = preset.configuration,
                        recentColors = (colors + state.recentColors).distinct().take(8),
                    )
                }
            }

            is WidgetDetailsAction.SaveCustomPreset -> {
                val name = action.name.trim().take(32)
                if (name.isBlank()) return
                val id = "custom-${name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')}-${System.currentTimeMillis()}"
                viewModelScope.launch {
                    presetRepository.save(
                        ClockWidgetPreset(
                            id = id,
                            name = name,
                            configuration = _uiState.value.configuration.copy(presetId = id),
                        ),
                    )
                }
            }

            is WidgetDetailsAction.DeleteCustomPreset -> viewModelScope.launch {
                presetRepository.delete(action.presetId)
            }

            is WidgetDetailsAction.SelectSection -> _uiState.update {
                it.copy(selectedSection = action.section)
            }

            is WidgetDetailsAction.SelectMoreSection -> _uiState.update {
                it.copy(selectedMoreSection = action.section)
            }

            is WidgetDetailsAction.SelectAnalogSection -> _uiState.update {
                it.copy(selectedAnalogSection = action.section)
            }

            is WidgetDetailsAction.SetPreviewSizeClass -> _uiState.update {
                it.copy(previewSizeClass = action.sizeClass)
            }

            WidgetDetailsAction.ToggleFavorite -> {
                val id = _uiState.value.widgetId ?: return
                viewModelScope.launch { favoritesRepository.toggleFavorite(id) }
            }

            is WidgetDetailsAction.PinningCompleted -> _uiState.update {
                it.copy(pinningResult = action.result)
            }
        }
    }

    private fun updateConfiguration(
        transform: DigitalClockConfiguration.() -> DigitalClockConfiguration,
    ) {
        _uiState.update { state -> state.copy(configuration = state.configuration.transform()) }
    }
}






