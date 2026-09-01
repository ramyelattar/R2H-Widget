package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockMonthStyle
import com.r2h_widget.widget.clock.ClockTimeAlignment
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.ClockYearStyle
import com.r2h_widget.widget.clock.DigitalClockConfiguration

/**
 * Digital Clock studio editors. These remain clock-specific; background and actions
 * reuse the sections shared with every other product.
 */

@Composable
fun TimeEditor(
    configuration: DigitalClockConfiguration,
    recentColors: List<String>,
    setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit),
) {
    val content = configuration.content
    val typography = configuration.typography
    EditorCard("Time", "Make the time feel like yours.") {
        ToggleControl("24-hour format", content.use24HourFormat) { value ->
            setConfiguration { it.copy(content = it.content.copy(use24HourFormat = value)) }
        }
        if (!content.use24HourFormat) {
            ToggleControl("Show AM / PM", content.showAmPm) { value ->
                setConfiguration { it.copy(content = it.content.copy(showAmPm = value)) }
            }
            ToggleControl("Leading zero", content.leadingZero) { value ->
                setConfiguration { it.copy(content = it.content.copy(leadingZero = value)) }
            }
        }
        ToggleControl("Show seconds", content.showSeconds) { value ->
            setConfiguration { it.copy(content = it.content.copy(showSeconds = value)) }
        }
        EnumProperty(
            label = "Format",
            value = content.separator.readableStudioLabel(),
            selected = content.separator,
            options = com.r2h_widget.widget.clock.ClockTimeSeparator.entries,
            optionLabel = { it.readableStudioLabel() },
            onSelected = { value -> setConfiguration { it.copy(content = it.content.copy(separator = value)) } },
        )
        EnumProperty(
            label = "Alignment",
            value = content.alignment.readableStudioLabel(),
            selected = content.alignment,
            options = ClockTimeAlignment.entries,
            optionLabel = { it.readableStudioLabel() },
            onSelected = { value -> setConfiguration { it.copy(content = it.content.copy(alignment = value)) } },
        )
        FontProperty("Font", typography.timeFontFamily) { value ->
            setConfiguration { it.copy(typography = it.typography.copy(timeFontFamily = value)) }
        }
        SliderControl("Size", typography.timeScale, 0.65f..1.45f, ::formatStudioScale, step = 0.05f) { value ->
            setConfiguration { it.copy(typography = it.typography.copy(timeScale = value)) }
        }
        ColorProperty("Time color", typography.timeColorHex, recentColors) { value ->
            setConfiguration { it.copy(typography = it.typography.copy(timeColorHex = value)) }
        }
    }
}

@Composable
fun DateEditor(
    configuration: DigitalClockConfiguration,
    recentColors: List<String>,
    setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit),
) {
    val date = configuration.date
    val typography = configuration.typography
    var customize by remember { mutableStateOf(false) }
    EditorCard("Date", "Keep the details subtle or make them part of the design.") {
        ToggleControl("Enable", date.enabled) { value ->
            setConfiguration { it.copy(date = it.date.copy(enabled = value)) }
        }
        if (date.enabled) {
            DateFormatProperty(configuration) { value ->
                setConfiguration { it.copy(date = it.date.copy(formatPreset = value)) }
            }
            EnumProperty(
                label = "Position",
                value = date.placement.readableStudioLabel(),
                selected = date.placement,
                options = ClockDatePlacement.entries,
                optionLabel = { it.readableStudioLabel() },
                onSelected = { value -> setConfiguration { it.copy(date = it.date.copy(placement = value)) } },
            )
            FontProperty("Font", typography.dateFontFamily) { value ->
                setConfiguration { it.copy(typography = it.typography.copy(dateFontFamily = value)) }
            }
            SliderControl("Size", typography.dateScale, 0.18f..0.62f, ::formatStudioScale, step = 0.02f) { value ->
                setConfiguration { it.copy(typography = it.typography.copy(dateScale = value)) }
            }
            ColorProperty("Date color", typography.dateColorHex, recentColors) { value ->
                setConfiguration { it.copy(typography = it.typography.copy(dateColorHex = value)) }
            }
            SliderControl("Opacity", typography.dateOpacity, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
                setConfiguration { it.copy(typography = it.typography.copy(dateOpacity = value)) }
            }
            ToggleProperty("Customize format", customize) { customize = it }
            if (customize) {
                EnumProperty(
                    label = "Weekday",
                    value = date.weekdayStyle.readableStudioLabel(),
                    selected = date.weekdayStyle,
                    options = ClockWeekdayStyle.entries,
                    optionLabel = { it.readableStudioLabel() },
                    onSelected = { value -> setConfiguration { it.copy(date = it.date.copy(weekdayStyle = value)) } },
                )
                EnumProperty(
                    label = "Month",
                    value = date.monthStyle.readableStudioLabel(),
                    selected = date.monthStyle,
                    options = ClockMonthStyle.entries,
                    optionLabel = { it.readableStudioLabel() },
                    onSelected = { value -> setConfiguration { it.copy(date = it.date.copy(monthStyle = value)) } },
                )
                EnumProperty(
                    label = "Year",
                    value = date.yearStyle.readableStudioLabel(),
                    selected = date.yearStyle,
                    options = ClockYearStyle.entries,
                    optionLabel = { it.readableStudioLabel() },
                    onSelected = { value -> setConfiguration { it.copy(date = it.date.copy(yearStyle = value)) } },
                )
                EnumProperty(
                    label = "Alignment",
                    value = date.alignment.readableStudioLabel(),
                    selected = date.alignment,
                    options = ClockDateAlignment.entries,
                    optionLabel = { it.readableStudioLabel() },
                    onSelected = { value -> setConfiguration { it.copy(date = it.date.copy(alignment = value)) } },
                )
            }
        }
    }
}

@Composable
fun LayoutEditor(
    configuration: DigitalClockConfiguration,
    setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit),
) {
    val layout = configuration.layout
    EditorCard("Layout", "Set the rhythm and balance of the composition.") {
        EnumProperty(
            label = "Alignment",
            value = layout.alignment.readableStudioLabel(),
            selected = layout.alignment,
            options = ClockTimeAlignment.entries,
            optionLabel = { it.readableStudioLabel() },
            onSelected = { value -> setConfiguration { it.copy(layout = it.layout.copy(alignment = value)) } },
        )
        SliderControl("Padding", layout.contentPaddingDp, 4f..32f, ::formatStudioNumber, step = 1f) { value ->
            setConfiguration { it.copy(layout = it.layout.copy(contentPaddingDp = value)) }
        }
        SliderControl("Time / date gap", layout.timeDateGapDp, 0f..24f, ::formatStudioNumber, step = 1f) { value ->
            setConfiguration { it.copy(layout = it.layout.copy(timeDateGapDp = value)) }
        }
    }
}

@Composable
fun AdvancedEditor(
    configuration: DigitalClockConfiguration,
    setConfiguration: (((DigitalClockConfiguration) -> DigitalClockConfiguration) -> Unit),
) {
    val layout = configuration.layout
    val background = configuration.background
    val typography = configuration.typography
    val responsive = configuration.responsive
    EditorCard("Advanced", "Fine-tune the details when you want to.") {
        Text("Position", color = com.r2h_widget.ui.theme.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        SliderControl("Horizontal offset", layout.horizontalOffsetDp, -24f..24f, { formatStudioNumber(it, signed = true) }, step = 1f) { value ->
            setConfiguration { it.copy(layout = it.layout.copy(horizontalOffsetDp = value)) }
        }
        SliderControl("Vertical offset", layout.verticalOffsetDp, -24f..24f, { formatStudioNumber(it, signed = true) }, step = 1f) { value ->
            setConfiguration { it.copy(layout = it.layout.copy(verticalOffsetDp = value)) }
        }
        Text("Typography", color = com.r2h_widget.ui.theme.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        SliderControl("Letter spacing", typography.letterSpacing, -0.05f..0.14f, { "${formatStudioNumber(it * 100f, signed = true)}%" }, step = 0.01f) { value ->
            setConfiguration { it.copy(typography = it.typography.copy(letterSpacing = value)) }
        }
        Text("Surface details", color = com.r2h_widget.ui.theme.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        SliderControl("Corner radius", background.cornerRadiusDp, 0f..48f, ::formatStudioNumber, step = 1f) { value ->
            setConfiguration { it.copy(background = it.background.copy(cornerRadiusDp = value)) }
        }
        SliderControl("Border width", background.borderWidthDp, 0f..3f, { "${formatStudioNumber(it)} dp" }, step = 0.5f) { value ->
            setConfiguration { it.copy(background = it.background.copy(borderWidthDp = value)) }
        }
        Text("Responsive sizing", color = com.r2h_widget.ui.theme.TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        ToggleControl("Show date in compact size", responsive.compactShowDate) { value ->
            setConfiguration { it.copy(responsive = it.responsive.copy(compactShowDate = value)) }
        }
        SliderControl("Compact scale", responsive.compactScale, 0.55f..1f, ::formatStudioScale, step = 0.05f) { value ->
            setConfiguration { it.copy(responsive = it.responsive.copy(compactScale = value)) }
        }
        SliderControl("Medium scale", responsive.mediumScale, 0.75f..1.2f, ::formatStudioScale, step = 0.05f) { value ->
            setConfiguration { it.copy(responsive = it.responsive.copy(mediumScale = value)) }
        }
        SliderControl("Expanded scale", responsive.expandedScale, 0.9f..1.4f, ::formatStudioScale, step = 0.05f) { value ->
            setConfiguration { it.copy(responsive = it.responsive.copy(expandedScale = value)) }
        }
    }
}

@Composable
private fun ToggleProperty(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) =
    ToggleControl(label, checked, onCheckedChange)
