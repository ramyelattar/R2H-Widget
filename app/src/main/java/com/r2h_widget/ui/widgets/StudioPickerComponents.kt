package com.r2h_widget.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.graphics.toColorInt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.r2h_widget.ui.theme.AppCorners
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.AccentViolet
import com.r2h_widget.ui.theme.GlassOutline
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.GlassSurfaceStrong
import com.r2h_widget.ui.theme.TextMuted
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.ui.theme.VoidBlack
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockFormatter
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.common.WidgetActionType
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StudioPropertyRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    leadingColorHex: String? = null,
    sample: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(AppCorners.button))
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        if (leadingColorHex != null) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(parseStudioColor(leadingColorHex))
                    .semantics { contentDescription = "Color swatch" },
            )
        }
        Text(label, color = TextPrimary, modifier = Modifier.weight(1f))
        if (sample != null) sample()
        Text(value, color = TextSecondary)
        Text("›", color = TextMuted, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ColorPickerSheet(
    title: String,
    initialHex: String,
    recentColors: List<String>,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var hex by remember(initialHex) { mutableStateOf(normalizeHex(initialHex)) }
    val initialHsv = remember(initialHex) { hsvFor(initialHex) }
    var hue by remember(initialHex) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(initialHex) { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember(initialHex) { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember(initialHex) { mutableFloatStateOf(initialHsv[3]) }

    fun setColor(h: Float, s: Float, v: Float, a: Float) {
        hue = h
        saturation = s
        brightness = v
        alpha = a
        hex = formatStudioHex(h, s, v, a)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSheet,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xxl)
                .padding(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(title, color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            if (recentColors.isNotEmpty()) {
                Text("Recent", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    recentColors.take(8).forEach { swatch ->
                        ColorSwatch(
                            value = swatch,
                            selected = hex.equals(swatch, ignoreCase = true),
                            onClick = {
                                val hsv = hsvFor(swatch)
                                setColor(hsv[0], hsv[1], hsv[2], hsv[3])
                            },
                        )
                    }
                }
            }
            Text("Hue", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            Slider(
                value = hue,
                onValueChange = { setColor(it, saturation, brightness, alpha) },
                valueRange = 0f..360f,
            )
            Text("Brightness", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            Slider(
                value = brightness,
                onValueChange = { setColor(hue, saturation, it, alpha) },
                valueRange = 0f..1f,
            )
            Text("Opacity", color = TextSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            Slider(
                value = alpha,
                onValueChange = { setColor(hue, saturation, brightness, it) },
                valueRange = 0f..1f,
            )
            OutlinedTextField(
                value = hex,
                onValueChange = { hex = it.take(9).uppercase(Locale.US) },
                label = { Text("HEX") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    val hsv = hsvFor(initialHex)
                    setColor(hsv[0], hsv[1], hsv[2], hsv[3])
                }) { Text("Reset") }
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (parseStudioColorOrNull(hex) != null) onApply(normalizeHex(hex))
                            onDismiss()
                        },
                    ) { Text("Apply") }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(value: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(parseStudioColor(value))
            .clickable(onClick = onClick)
            .borderStudio(selected)
            .semantics { contentDescription = "Color $value" },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T> StudioChoiceSheet(
    title: String,
    initialValue: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onDismiss: () -> Unit,
    onApply: (T) -> Unit,
    optionSample: (@Composable (T) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember(initialValue) { mutableStateOf(initialValue) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSheet,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xxl)
                .padding(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(title, color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                items(options) { option ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppCorners.button))
                            .clickable { selected = option }
                            .background(if (isSelected) AccentViolet.copy(alpha = 0.16f) else Color.Transparent)
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        if (optionSample != null) optionSample(option)
                        Text(optionLabel(option), color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(if (isSelected) "✓" else "", color = AccentViolet)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                Button(
                    onClick = {
                        onApply(selected)
                        onDismiss()
                    },
                    modifier = Modifier.padding(start = AppSpacing.sm),
                ) { Text("Apply") }
            }
        }
    }
}

@Composable
fun FontPickerSheet(
    initialValue: ClockFontFamily,
    onDismiss: () -> Unit,
    onApply: (ClockFontFamily) -> Unit,
) {
    StudioChoiceSheet(
        title = "Choose font",
        initialValue = initialValue,
        options = ClockFontFamily.entries,
        optionLabel = { it.label },
        onDismiss = onDismiss,
        onApply = onApply,
        optionSample = { family ->
            Text(
                text = "19:56",
                color = TextPrimary,
                fontFamily = family.toComposeFontFamily(),
                fontWeight = FontWeight.Medium,
            )
        },
    )
}

@Composable
fun DateFormatPickerSheet(
    configuration: DigitalClockConfiguration,
    initialValue: ClockDateFormatPreset,
    onDismiss: () -> Unit,
    onApply: (ClockDateFormatPreset) -> Unit,
) {
    StudioChoiceSheet(
        title = "Date format",
        initialValue = initialValue,
        options = ClockDateFormatPreset.entries,
        optionLabel = { it.label },
        onDismiss = onDismiss,
        onApply = onApply,
        optionSample = { preset ->
            val preview = ClockFormatter.format(
                instant = Instant.now(),
                configuration = configuration.copy(date = configuration.date.copy(formatPreset = preset)),
                zoneId = ZoneId.systemDefault(),
                locale = Locale.getDefault(),
            )
            Text(
                text = listOf(preview.weekday, preview.date).joinToString(" · "),
                color = TextSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
        },
    )
}

@Composable
fun ActionPickerSheet(
    initialValue: WidgetActionType,
    onDismiss: () -> Unit,
    onApply: (WidgetActionType) -> Unit,
) {
    StudioChoiceSheet(
        title = "Tap action",
        initialValue = initialValue,
        options = WidgetActionType.entries,
        optionLabel = { it.readableStudioLabel() },
        onDismiss = onDismiss,
        onApply = onApply,
    )
}

fun WidgetActionType.readableStudioLabel(): String = when (this) {
    WidgetActionType.NONE -> "None"
    WidgetActionType.OPEN_APP -> "Open this app"
    WidgetActionType.OPEN_ALARM -> "Open alarm"
    WidgetActionType.OPEN_CALENDAR -> "Open calendar"
    WidgetActionType.OPEN_INSTALLED_APP -> "Open installed app"
}

private val ObsidianSheet = Color(0xFF101016)

private fun Modifier.borderStudio(selected: Boolean): Modifier = border(
    width = if (selected) 2.dp else 1.dp,
    color = if (selected) TextPrimary else GlassOutline,
    shape = CircleShape,
)

private fun normalizeHex(value: String): String =
    if (parseStudioColorOrNull(value) != null) value.uppercase(Locale.US) else "#A78BFA"

private fun parseStudioColorOrNull(value: String): Int? = runCatching {
    value.toColorInt()
}.getOrNull()

private fun parseStudioColor(value: String): Color = Color(
    parseStudioColorOrNull(value) ?: android.graphics.Color.rgb(167, 139, 250),
)

private fun hsvFor(value: String): FloatArray {
    val color = parseStudioColorOrNull(value) ?: android.graphics.Color.rgb(167, 139, 250)
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color, hsv)
    return floatArrayOf(hsv[0], hsv[1], hsv[2], android.graphics.Color.alpha(color) / 255f)
}

private fun formatStudioHex(hue: Float, saturation: Float, brightness: Float, alpha: Float): String {
    val color = android.graphics.Color.HSVToColor(
        (alpha.coerceIn(0f, 1f) * 255).roundToInt(),
        floatArrayOf(hue, saturation, brightness),
    )
    return if (alpha >= 0.999f) {
        "#%06X".format(Locale.US, color and 0xFFFFFF)
    } else {
        "#%08X".format(Locale.US, color)
    }
}
