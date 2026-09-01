package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.weather.WeatherUnits
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetLayout

/**
 * Weather studio: readout toggles, layout, colors, and the shared background/action
 * sections. The provider status banner is explicit — the widget shows live data only
 * once a weather source is connected.
 */
@Composable
fun WeatherWidgetEditor(
    configuration: WeatherWidgetConfiguration,
    recentColors: List<String>,
    setConfiguration: (WeatherWidgetConfiguration) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Surface(
            color = com.r2h_widget.ui.theme.GlassSurfaceStrong,
            shape = RoundedCornerShape(AppCorners.card),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = Modifier.padding(AppSpacing.lg)) {
                Column {
                    Text(
                        "No weather source connected",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "The widget shows a clear unavailable state until a provider is added. " +
                            "Everything you style here applies the moment live data arrives.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                }
            }
        }

        EditorCard("Readout", "Choose what the widget displays.") {
            ToggleControl("Show temperature", configuration.showTemperature) { value ->
                setConfiguration(configuration.copy(showTemperature = value))
            }
            ToggleControl("Show condition", configuration.showCondition) { value ->
                setConfiguration(configuration.copy(showCondition = value))
            }
            ToggleControl("Show location", configuration.showLocation) { value ->
                setConfiguration(configuration.copy(showLocation = value))
            }
            ToggleControl("Show high / low", configuration.showHighLow) { value ->
                setConfiguration(configuration.copy(showHighLow = value))
            }
            if (configuration.showHighLow) {
                Text(
                    "High / low appears once the connected provider includes a forecast.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            ToggleControl("Show icon", configuration.showIcon) { value ->
                setConfiguration(configuration.copy(showIcon = value))
            }
            EnumProperty(
                label = "Units",
                value = configuration.units.label,
                selected = configuration.units,
                options = WeatherUnits.entries,
                optionLabel = { it.label },
                onSelected = { value -> setConfiguration(configuration.copy(units = value)) },
            )
            TextProperty(
                label = "Location label (optional)",
                value = configuration.locationName.orEmpty(),
                onValueChange = { value -> setConfiguration(configuration.copy(locationName = value.ifBlank { null })) },
            )
        }

        EditorCard("Layout", "Pick the arrangement that fits your cell.") {
            ModeChipRow(
                options = WeatherWidgetLayout.entries,
                selected = configuration.layout,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(layout = value)) },
            )
            SliderControl("Text size", configuration.textScale, 0.7f..1.4f, ::formatStudioScale, step = 0.05f) { value ->
                setConfiguration(configuration.copy(textScale = value))
            }
            SliderControl("Padding", configuration.contentPaddingDp, 4f..32f, ::formatStudioNumber, step = 1f) { value ->
                setConfiguration(configuration.copy(contentPaddingDp = value))
            }
            ToggleControl("Align start", configuration.alignStart) { value ->
                setConfiguration(configuration.copy(alignStart = value))
            }
        }

        EditorCard("Colors", "Tint the readout for your wallpaper.") {
            ColorProperty("Temperature color", configuration.temperatureColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(temperatureColorHex = value))
            }
            ColorProperty("Secondary color", configuration.secondaryColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(secondaryColorHex = value))
            }
            ColorProperty("Icon tint", configuration.iconTintHex, recentColors) { value ->
                setConfiguration(configuration.copy(iconTintHex = value))
            }
        }

        BackgroundEditorSection(
            background = configuration.background,
            effects = com.r2h_widget.widget.common.WidgetEffectsConfiguration(),
            recentColors = recentColors,
            showEffects = false,
            setBackground = { value -> setConfiguration(configuration.copy(background = value)) },
            setEffects = { },
        )
        ActionsEditorSection(
            action = configuration.action,
            setAction = { value -> setConfiguration(configuration.copy(action = value)) },
        )
    }
}
