package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetLayout

/**
 * Battery studio: readout toggles, layout mode, per-state colors, and the shared
 * background/action sections. The preview always mirrors the device's real battery
 * state — there is no placeholder percentage.
 */
@Composable
fun BatteryWidgetEditor(
    configuration: BatteryWidgetConfiguration,
    recentColors: List<String>,
    setConfiguration: (BatteryWidgetConfiguration) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        EditorCard("Readout", "Choose what the widget tells you at a glance.") {
            ToggleControl("Show percentage", configuration.showPercentage) { value ->
                setConfiguration(configuration.copy(showPercentage = value))
            }
            ToggleControl("Show charging state", configuration.showChargingState) { value ->
                setConfiguration(configuration.copy(showChargingState = value))
            }
            ToggleControl("Show power source", configuration.showPlugSource) { value ->
                setConfiguration(configuration.copy(showPlugSource = value))
            }
            ToggleControl("Show charging icon", configuration.showIcon) { value ->
                setConfiguration(configuration.copy(showIcon = value))
            }
            Text(
                "The icon appears while the device is charging.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        EditorCard("Layout", "Pick the arrangement that fits your cell.") {
            ModeChipRow(
                options = BatteryWidgetLayout.entries,
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

        EditorCard("Colors", "Tint the readout for normal, low, and charging states.") {
            ColorProperty("Percentage color", configuration.percentColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(percentColorHex = value))
            }
            ColorProperty("Status color", configuration.statusColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(statusColorHex = value))
            }
            ColorProperty("Progress color", configuration.progressColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(progressColorHex = value))
            }
            ColorProperty("Low battery color", configuration.lowBatteryColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(lowBatteryColorHex = value))
            }
            ColorProperty("Charging color", configuration.chargingColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(chargingColorHex = value))
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
