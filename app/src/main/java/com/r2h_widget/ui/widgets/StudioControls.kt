package com.r2h_widget.ui.widgets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetActionType
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetBackgroundShape
import com.r2h_widget.widget.common.WidgetEffectsConfiguration
import kotlin.math.roundToInt

/**
 * Shared studio control vocabulary used by every product editor: the same rows,
 * pickers, and background/action sections across Digital Clock, Analog Clock,
 * Battery, Weather, and Search.
 */

@Composable
fun EditorCard(title: String, description: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GlassSurface,
        shape = RoundedCornerShape(AppCorners.card),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = GlassOutline, modifier = Modifier.padding(vertical = AppSpacing.xs))
            content()
        }
    }
}

@Composable
fun ToggleControl(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun <T> EnumProperty(
    label: String,
    value: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    var sheetVisible by remember { mutableStateOf(false) }
    StudioPropertyRow(label, value, onClick = { sheetVisible = true })
    if (sheetVisible) {
        StudioChoiceSheet(
            title = label,
            initialValue = selected,
            options = options,
            optionLabel = optionLabel,
            onDismiss = { sheetVisible = false },
            onApply = onSelected,
        )
    }
}

@Composable
fun ColorProperty(
    label: String,
    value: String,
    recentColors: List<String>,
    onSelected: (String) -> Unit,
) {
    var sheetVisible by remember { mutableStateOf(false) }
    StudioPropertyRow(
        label = label,
        value = value.uppercase(),
        leadingColorHex = value,
        onClick = { sheetVisible = true },
    )
    if (sheetVisible) {
        ColorPickerSheet(
            title = label,
            initialHex = value,
            recentColors = recentColors,
            onDismiss = { sheetVisible = false },
            onApply = onSelected,
        )
    }
}

@Composable
fun SliderControl(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    formatter: (Float) -> String,
    step: Float? = null,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextPrimary)
            Text(formatter(value), color = TextSecondary)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = { raw ->
                val snapped = step?.let {
                    (range.start + ((raw - range.start) / it).roundToInt() * it)
                        .coerceIn(range.start, range.endInclusive)
                } ?: raw
                onValueChange(snapped)
            },
            valueRange = range,
            steps = step?.let { ((range.endInclusive - range.start) / it).roundToInt() - 1 }?.coerceAtLeast(0) ?: 0,
        )
    }
}

/** Text-field property for short inline strings such as the search hint. */
@Composable
fun TextProperty(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(40)) },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Background (and optional effects) section shared by every product. */
@Composable
fun BackgroundEditorSection(
    background: WidgetBackgroundConfiguration,
    effects: WidgetEffectsConfiguration,
    recentColors: List<String>,
    showEffects: Boolean,
    setBackground: (WidgetBackgroundConfiguration) -> Unit,
    setEffects: (WidgetEffectsConfiguration) -> Unit,
) {
    EditorCard("Background", "Give the widget a surface that fits your wallpaper.") {
        EnumProperty(
            label = "Fill",
            value = background.fill.readableStudioLabel(),
            selected = background.fill,
            options = WidgetBackgroundFill.entries,
            optionLabel = { it.readableStudioLabel() },
            onSelected = { value -> setBackground(background.copy(fill = value)) },
        )
        EnumProperty(
            label = "Shape",
            value = background.shape.readableStudioLabel(),
            selected = background.shape,
            options = WidgetBackgroundShape.entries,
            optionLabel = { it.readableStudioLabel() },
            onSelected = { value -> setBackground(background.copy(shape = value)) },
        )
        SliderControl("Opacity", background.opacity, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
            setBackground(background.copy(opacity = value))
        }
        when (background.fill) {
            WidgetBackgroundFill.TRANSPARENT -> Unit
            WidgetBackgroundFill.SOLID -> ColorProperty("Color", background.solidColorHex, recentColors) { value ->
                setBackground(background.copy(solidColorHex = value))
            }
            WidgetBackgroundFill.LINEAR_GRADIENT,
            WidgetBackgroundFill.RADIAL_GRADIENT,
            -> {
                ColorProperty("Start color", background.gradientStartHex, recentColors) { value ->
                    setBackground(background.copy(gradientStartHex = value))
                }
                ColorProperty("End color", background.gradientEndHex, recentColors) { value ->
                    setBackground(background.copy(gradientEndHex = value))
                }
                if (background.fill == WidgetBackgroundFill.LINEAR_GRADIENT) {
                    SliderControl("Direction", background.gradientAngleDegrees, 0f..360f, { "${formatStudioNumber(it)}°" }, step = 5f) { value ->
                        setBackground(background.copy(gradientAngleDegrees = value))
                    }
                }
            }
        }
        ToggleControl("Border", background.borderEnabled) { value ->
            setBackground(background.copy(borderEnabled = value))
        }
        if (background.borderEnabled) {
            ColorProperty("Border color", background.borderColorHex, recentColors) { value ->
                setBackground(background.copy(borderColorHex = value))
            }
            SliderControl("Border opacity", background.borderOpacity, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
                setBackground(background.copy(borderOpacity = value))
            }
        }
        ToggleControl("Highlight", background.highlightEnabled) { value ->
            setBackground(background.copy(highlightEnabled = value))
        }
        if (background.highlightEnabled) {
            ColorProperty("Highlight color", background.highlightColorHex, recentColors) { value ->
                setBackground(background.copy(highlightColorHex = value))
            }
            SliderControl("Highlight strength", background.highlightOpacity, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
                setBackground(background.copy(highlightOpacity = value))
            }
        }
        if (showEffects) {
            ToggleControl("Glow", effects.glowEnabled) { value ->
                setEffects(effects.copy(glowEnabled = value))
            }
            if (effects.glowEnabled) {
                ColorProperty("Glow color", effects.glowColorHex, recentColors) { value ->
                    setEffects(effects.copy(glowColorHex = value))
                }
                SliderControl("Strength", effects.glowStrength, 0f..1f, ::formatStudioPercent, step = 0.05f) { value ->
                    setEffects(effects.copy(glowStrength = value))
                }
            }
        }
    }
}

/** Actions section shared by every product. */
@Composable
fun ActionsEditorSection(
    action: WidgetActionConfiguration,
    setAction: (WidgetActionConfiguration) -> Unit,
) {
    EditorCard("Actions", "Choose what happens when you tap the widget.") {
        var actionSheet by remember { mutableStateOf(false) }
        StudioPropertyRow("Tap action", action.type.readableStudioLabel(), onClick = { actionSheet = true })
        if (action.type == WidgetActionType.OPEN_INSTALLED_APP) {
            OutlinedTextField(
                value = action.packageName.orEmpty(),
                onValueChange = { value ->
                    setAction(action.copy(packageName = value.ifBlank { null }))
                },
                label = { Text("App package") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (actionSheet) {
            ActionPickerSheet(
                initialValue = action.type,
                onDismiss = { actionSheet = false },
                onApply = { value -> setAction(action.copy(type = value)) },
            )
        }
    }
}

/** Row of chips used to pick a layout or mode inside an editor card. */
@Composable
fun <T> ModeChipRow(
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelected(option) },
                label = { Text(optionLabel(option), maxLines = 1) },
            )
        }
    }
}

fun Enum<*>.readableStudioLabel(): String = name
    .lowercase()
    .replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
