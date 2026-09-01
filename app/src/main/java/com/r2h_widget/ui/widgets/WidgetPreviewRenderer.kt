package com.r2h_widget.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.catalog.WidgetPreviewRendererRegistry
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetState
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.defaultClockConfiguration
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration

/**
 * One preview renderer per product, resolved by the stable renderer registry key.
 * Battery previews receive the caller's real [BatteryWidgetState]; every other
 * product renders from its typed configuration.
 */
@Composable
fun WidgetPreviewRenderer(
    definition: WidgetDefinition,
    modifier: Modifier = Modifier,
    configuration: DigitalClockConfiguration = definition.defaultClockConfiguration(),
    analogConfiguration: AnalogClockConfiguration = AnalogClockConfiguration(),
    batteryConfiguration: BatteryWidgetConfiguration = BatteryWidgetConfiguration(),
    batteryState: BatteryWidgetState? = null,
    weatherConfiguration: WeatherWidgetConfiguration = WeatherWidgetConfiguration(),
    searchConfiguration: SearchWidgetConfiguration = SearchWidgetConfiguration(),
    musicConfiguration: MusicWidgetConfiguration = MusicWidgetConfiguration(),
    sizeClass: WidgetSizeClass = WidgetSizeClass.MEDIUM,
    live: Boolean = true,
) {
    val accent = runCatching { Color(configuration.accent.colorHex.toColorInt()) }
        .getOrDefault(Color(0xFFA78BFA))
    when (WidgetPreviewRendererRegistry.resolve(definition.rendererKey)) {
        WidgetPreviewRendererRegistry.DIGITAL_CLOCK -> DigitalClockPreview(
            definition = definition,
            modifier = modifier,
            accent = accent,
            configuration = configuration,
            sizeClass = sizeClass,
            live = live,
        )

        WidgetPreviewRendererRegistry.ANALOG_CLOCK -> AnalogClockPreview(
            modifier = modifier,
            configuration = analogConfiguration,
            live = live,
        )

        WidgetPreviewRendererRegistry.BATTERY -> BatteryPreview(
            modifier = modifier,
            configuration = batteryConfiguration,
            state = batteryState ?: defaultBatteryPreviewState(),
        )

        WidgetPreviewRendererRegistry.WEATHER -> WeatherPreview(
            modifier = modifier,
            configuration = weatherConfiguration,
        )

        WidgetPreviewRendererRegistry.SEARCH -> SearchPreview(
            modifier = modifier,
            configuration = searchConfiguration,
        )

        WidgetPreviewRendererRegistry.MUSIC -> MusicWidgetPreview(
            modifier = modifier,
            configuration = musicConfiguration,
        )

        else -> Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.24f), Color.Transparent),
                    ),
                ),
        ) { FallbackPreview(definition = definition) }
    }
}

@Composable
private fun defaultBatteryPreviewState(): BatteryWidgetState = BatteryWidgetState(
    levelPercent = 73,
    isCharging = false,
    plugSource = com.r2h_widget.widget.battery.BatteryPlugSource.UNKNOWN,
    isLow = false,
)



