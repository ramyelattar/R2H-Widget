package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.r2h_widget.ui.AnalogStudioSection
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.analog.AnalogCapStyle
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.analog.AnalogMarkerStyle
import com.r2h_widget.widget.analog.AnalogNumberStyle
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockMonthStyle
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.ClockYearStyle

/**
 * Analog Clock studio: dial, markers, numerals, hands, cap, date, layout, and the
 * shared background/action sections. Appearance lives here as controls — the catalog
 * keeps exactly one analog product.
 */
@Composable
fun AnalogClockSectionTabs(
    selected: AnalogStudioSection,
    onSelected: (AnalogStudioSection) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = "Studio sections" },
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        AnalogStudioSection.entries.forEach { section ->
            StudioTab(
                label = section.label,
                selected = selected == section,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(section) },
            )
        }
    }
}

@Composable
fun AnalogClockEditor(
    section: AnalogStudioSection,
    configuration: AnalogClockConfiguration,
    recentColors: List<String>,
    setConfiguration: (AnalogClockConfiguration) -> Unit,
) {
    when (section) {
        AnalogStudioSection.FACE -> AnalogFaceEditor(configuration, recentColors, setConfiguration)
        AnalogStudioSection.HANDS -> AnalogHandsEditor(configuration, recentColors, setConfiguration)
        AnalogStudioSection.DATE -> AnalogDateEditor(configuration, setConfiguration)
        AnalogStudioSection.LAYOUT -> AnalogLayoutEditor(configuration, setConfiguration)
        AnalogStudioSection.MORE -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            BackgroundEditorSection(
                background = configuration.background,
                effects = configuration.effects,
                recentColors = recentColors,
                showEffects = true,
                setBackground = { value -> setConfiguration(configuration.copy(background = value)) },
                setEffects = { value -> setConfiguration(configuration.copy(effects = value)) },
            )
            ActionsEditorSection(
                action = configuration.action,
                setAction = { value -> setConfiguration(configuration.copy(action = value)) },
            )
        }
    }
}

@Composable
private fun AnalogFaceEditor(
    configuration: AnalogClockConfiguration,
    recentColors: List<String>,
    setConfiguration: (AnalogClockConfiguration) -> Unit,
) {
    EditorCard("Dial", "Shape the face of the clock.") {
        ToggleControl("Hour markers", configuration.showHourMarkers) { value ->
            setConfiguration(configuration.copy(showHourMarkers = value))
        }
        ToggleControl("Minute markers", configuration.showMinuteMarkers) { value ->
            setConfiguration(configuration.copy(showMinuteMarkers = value))
        }
        if (configuration.showHourMarkers || configuration.showMinuteMarkers) {
            EnumProperty(
                label = "Marker style",
                value = configuration.markerStyle.label,
                selected = configuration.markerStyle,
                options = AnalogMarkerStyle.entries,
                optionLabel = { it.label },
                onSelected = { value -> setConfiguration(configuration.copy(markerStyle = value)) },
            )
            ColorProperty("Marker color", configuration.markerColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(markerColorHex = value))
            }
        }
        EnumProperty(
            label = "Numbers",
            value = configuration.numberStyle.label,
            selected = configuration.numberStyle,
            options = AnalogNumberStyle.entries,
            optionLabel = { it.label },
            onSelected = { value -> setConfiguration(configuration.copy(numberStyle = value)) },
        )
        if (configuration.numberStyle != AnalogNumberStyle.NONE) {
            ColorProperty("Number color", configuration.numberColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(numberColorHex = value))
            }
        }
        ToggleControl("Dial plate", configuration.dialFillEnabled) { value ->
            setConfiguration(configuration.copy(dialFillEnabled = value))
        }
        if (configuration.dialFillEnabled) {
            ColorProperty("Plate color", configuration.dialColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(dialColorHex = value))
            }
        }
    }
}

@Composable
private fun AnalogHandsEditor(
    configuration: AnalogClockConfiguration,
    recentColors: List<String>,
    setConfiguration: (AnalogClockConfiguration) -> Unit,
) {
    EditorCard("Hands", "Pick which hands you see and how they look.") {
        ToggleControl("Hour hand", configuration.showHourHand) { value ->
            setConfiguration(configuration.copy(showHourHand = value))
        }
        if (configuration.showHourHand) {
            ColorProperty("Hour hand color", configuration.hourHandColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(hourHandColorHex = value))
            }
        }
        ToggleControl("Minute hand", configuration.showMinuteHand) { value ->
            setConfiguration(configuration.copy(showMinuteHand = value))
        }
        if (configuration.showMinuteHand) {
            ColorProperty("Minute hand color", configuration.minuteHandColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(minuteHandColorHex = value))
            }
        }
        ToggleControl("Second hand", configuration.showSecondHand) { value ->
            setConfiguration(configuration.copy(showSecondHand = value))
        }
        if (configuration.showSecondHand) {
            ColorProperty("Second hand color", configuration.secondHandColorHex, recentColors) { value ->
                setConfiguration(configuration.copy(secondHandColorHex = value))
            }
            Text(
                "The second hand moves with the widget's minute refresh; it may pause while the device sleeps.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        EnumProperty(
            label = "Center cap",
            value = configuration.capStyle.label,
            selected = configuration.capStyle,
            options = AnalogCapStyle.entries,
            optionLabel = { it.label },
            onSelected = { value -> setConfiguration(configuration.copy(capStyle = value)) },
        )
        ColorProperty("Cap color", configuration.capColorHex, recentColors) { value ->
            setConfiguration(configuration.copy(capColorHex = value))
        }
    }
}

@Composable
private fun AnalogDateEditor(
    configuration: AnalogClockConfiguration,
    setConfiguration: (AnalogClockConfiguration) -> Unit,
) {
    EditorCard("Date", "Add a small date readout beside the dial.") {
        ToggleControl("Show date", configuration.date.enabled) { value ->
            setConfiguration(configuration.copy(date = configuration.date.copy(enabled = value)))
        }
        if (configuration.date.enabled) {
            EnumProperty(
                label = "Position",
                value = configuration.date.placement.readableStudioLabel(),
                selected = configuration.date.placement,
                options = ClockDatePlacement.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(date = configuration.date.copy(placement = value))) },
            )
            EnumProperty(
                label = "Alignment",
                value = configuration.dateAlignment.readableStudioLabel(),
                selected = configuration.dateAlignment,
                options = ClockDateAlignment.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(dateAlignment = value)) },
            )
            EnumProperty(
                label = "Weekday",
                value = configuration.date.weekdayStyle.readableStudioLabel(),
                selected = configuration.date.weekdayStyle,
                options = ClockWeekdayStyle.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(date = configuration.date.copy(weekdayStyle = value))) },
            )
            EnumProperty(
                label = "Format",
                value = configuration.date.formatPreset.label,
                selected = configuration.date.formatPreset,
                options = ClockDateFormatPreset.entries,
                optionLabel = { it.label },
                onSelected = { value -> setConfiguration(configuration.copy(date = configuration.date.copy(formatPreset = value))) },
            )
            EnumProperty(
                label = "Month style",
                value = configuration.date.monthStyle.readableStudioLabel(),
                selected = configuration.date.monthStyle,
                options = ClockMonthStyle.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(date = configuration.date.copy(monthStyle = value))) },
            )
            EnumProperty(
                label = "Year",
                value = configuration.date.yearStyle.readableStudioLabel(),
                selected = configuration.date.yearStyle,
                options = ClockYearStyle.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration(configuration.copy(date = configuration.date.copy(yearStyle = value))) },
            )
            FontProperty("Date font", configuration.dateFontFamily) { value ->
                setConfiguration(configuration.copy(dateFontFamily = value))
            }
        }
    }
}

@Composable
private fun AnalogLayoutEditor(
    configuration: AnalogClockConfiguration,
    setConfiguration: (AnalogClockConfiguration) -> Unit,
) {
    EditorCard("Layout", "Set the size and breathing room of the dial.") {
        SliderControl("Dial size", configuration.contentScale, 0.6f..1.15f, ::formatStudioScale, step = 0.05f) { value ->
            setConfiguration(configuration.copy(contentScale = value))
        }
        SliderControl("Padding", configuration.contentPaddingDp, 4f..32f, ::formatStudioNumber, step = 1f) { value ->
            setConfiguration(configuration.copy(contentPaddingDp = value))
        }
        Text(
            "Background and tap behavior live in More.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
internal fun StudioTab(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = "Section $label"
                role = Role.Tab
            },
        color = if (selected) AccentViolet.copy(alpha = 0.2f) else com.r2h_widget.ui.theme.GlassSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppCorners.button),
        onClick = onClick,
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = label,
                color = if (selected) TextPrimary else TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}
