package com.r2h_widget.ui

import com.r2h_widget.MainDispatcherRule
import com.r2h_widget.widget.clock.ClockAccent
import com.r2h_widget.widget.clock.ClockPresetCatalog
import com.r2h_widget.data.InMemoryWidgetInstanceRepository
import com.r2h_widget.data.InMemoryTypedWidgetInstanceRepository
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import com.r2h_widget.data.WidgetInstance
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class WidgetDetailsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun validWidgetId_resolvesDefinitionAndDefaultConfiguration() {
        val viewModel = WidgetDetailsViewModel()

        viewModel.openWidget("digital-clock")

        assertEquals("digital-clock", viewModel.uiState.value.widgetId)
        assertNotNull(viewModel.uiState.value.definition)
        assertEquals(ClockAccent.VIOLET, viewModel.uiState.value.configuration.accent)
    }

    @Test
    fun invalidWidgetId_exposesControlledError() {
        val viewModel = WidgetDetailsViewModel()

        viewModel.openWidget("missing")

        assertNull(viewModel.uiState.value.definition)
        assertEquals("This widget is no longer available.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun legacyClockPresetId_opensTheCanonicalDigitalClock() {
        val viewModel = WidgetDetailsViewModel()

        viewModel.openWidget("clock-03")

        assertEquals("digital-clock", viewModel.uiState.value.widgetId)
        assertNotNull(viewModel.uiState.value.definition)
    }

    @Test
    fun configurationActions_updateImmutableDetailsState() {
        val viewModel = WidgetDetailsViewModel()
        viewModel.openWidget("digital-clock")

        viewModel.onAction(WidgetDetailsAction.Set24HourFormat(true))
        viewModel.onAction(WidgetDetailsAction.SetShowDate(false))
        viewModel.onAction(WidgetDetailsAction.SetShowWeekday(false))
        viewModel.onAction(WidgetDetailsAction.SetAccent(ClockAccent.CYAN))

        assertEquals(true, viewModel.uiState.value.configuration.use24HourFormat)
        assertEquals(false, viewModel.uiState.value.configuration.showDate)
        assertEquals(false, viewModel.uiState.value.configuration.showWeekday)
        assertEquals(ClockAccent.CYAN, viewModel.uiState.value.configuration.accent)
    }

    @Test
    fun applyingPreset_updatesTheDedicatedEditorState() {
        val viewModel = WidgetDetailsViewModel()
        viewModel.openWidget("digital-clock")

        viewModel.onAction(WidgetDetailsAction.ApplyPreset("neon-pulse"))

        assertEquals(
            ClockPresetCatalog.findById("neon-pulse")?.configuration,
            viewModel.uiState.value.configuration,
        )
    }

    @Test
    fun openingExistingInstance_loadsConfigurationByAppWidgetId() {
        val repository = InMemoryWidgetInstanceRepository()
        val saved = WidgetInstance(
            appWidgetId = 42,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(accent = ClockAccent.CYAN, use24HourFormat = true),
        )
        kotlinx.coroutines.runBlocking { repository.save(saved) }
        val viewModel = WidgetDetailsViewModel(widgetInstanceRepository = repository)

        viewModel.openWidget("digital-clock", appWidgetId = 42)

        assertEquals(42, viewModel.uiState.value.appWidgetId)
        assertEquals(saved.configuration, viewModel.uiState.value.configuration)
    }

    @Test
    fun openingExistingAnalogInstance_loadsTypedConfiguration() {
        val repository = InMemoryTypedWidgetInstanceRepository()
        val saved = TypedWidgetInstance(
            appWidgetId = 55,
            productId = "analog-clock",
            configuration = TypedWidgetConfiguration.Analog(AnalogClockConfiguration(showSecondHand = false)),
        )
        runBlocking { repository.save(saved) }
        val viewModel = WidgetDetailsViewModel(typedWidgetInstanceRepository = repository)

        viewModel.openWidget("analog-clock", appWidgetId = 55)

        assertEquals(false, viewModel.uiState.value.analogConfiguration.showSecondHand)
    }

    @Test
    fun studioNavigation_keepsPrimaryAndMoreSectionsSeparate() {
        val viewModel = WidgetDetailsViewModel()
        viewModel.openWidget("digital-clock")

        viewModel.onAction(WidgetDetailsAction.SelectSection(WidgetStudioSection.MORE))
        viewModel.onAction(WidgetDetailsAction.SelectMoreSection(WidgetStudioMoreSection.EFFECTS))

        assertEquals(WidgetStudioSection.MORE, viewModel.uiState.value.selectedSection)
        assertEquals(WidgetStudioMoreSection.EFFECTS, viewModel.uiState.value.selectedMoreSection)
    }

    @Test
    fun analogSectionNavigation_updatesSelectedAnalogSection() {
        val viewModel = WidgetDetailsViewModel()
        viewModel.openWidget("analog-clock")

        viewModel.onAction(WidgetDetailsAction.SelectAnalogSection(AnalogStudioSection.HANDS))

        assertEquals(AnalogStudioSection.HANDS, viewModel.uiState.value.selectedAnalogSection)
    }

    @Test
    fun typedConfigurationActions_updateTheirOwnState() {
        val viewModel = WidgetDetailsViewModel()
        viewModel.openWidget("battery")
        viewModel.onAction(
            WidgetDetailsAction.SetBatteryConfiguration(
                BatteryWidgetConfiguration(showPercentage = false, showPlugSource = true),
            ),
        )
        assertEquals(false, viewModel.uiState.value.batteryConfiguration.showPercentage)
        assertEquals(true, viewModel.uiState.value.batteryConfiguration.showPlugSource)

        viewModel.openWidget("search")
        viewModel.onAction(
            WidgetDetailsAction.SetSearchConfiguration(SearchWidgetConfiguration(hintText = "Look it up")),
        )
        assertEquals("Look it up", viewModel.uiState.value.searchConfiguration.hintText)
    }
}
