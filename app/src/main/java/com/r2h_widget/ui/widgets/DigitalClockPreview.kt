package com.r2h_widget.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.widget.clock.ClockFormatter
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockDatePlacement
import com.r2h_widget.widget.clock.ClockStyleResolver
import com.r2h_widget.widget.clock.ClockTimeAlignment
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetBackgroundShape
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.clock.defaultClockConfiguration
import java.time.Instant
import kotlinx.coroutines.delay

@Composable
fun DigitalClockPreview(
    definition: WidgetDefinition,
    modifier: Modifier = Modifier,
    accent: Color,
    configuration: DigitalClockConfiguration = definition.defaultClockConfiguration(),
    sizeClass: WidgetSizeClass = WidgetSizeClass.MEDIUM,
    live: Boolean = true,
) {
    var now by remember { mutableStateOf(Instant.now()) }
    if (live) {
        LaunchedEffect(configuration.content.showSeconds) {
            while (true) {
                now = Instant.now()
                delay(if (configuration.content.showSeconds) 1_000L else 30_000L)
            }
        }
    }
    val display = ClockFormatter.format(
        instant = now,
        configuration = configuration,
        zoneId = java.time.ZoneId.systemDefault(),
        locale = java.util.Locale.getDefault(),
    )
    val resolved = ClockStyleResolver.resolve(configuration, sizeClass)
    val background = configuration.background
    val effects = configuration.effects
    val backgroundColor = runCatching { Color(background.solidColorHex.toColorInt()) }
        .getOrDefault(Color(0xFF101017))
    val gradientStart = runCatching { Color(background.gradientStartHex.toColorInt()) }
        .getOrDefault(backgroundColor)
    val gradientEnd = runCatching { Color(background.gradientEndHex.toColorInt()) }
        .getOrDefault(Color.Black)
    val shape = when (background.shape) {
        WidgetBackgroundShape.PILL -> RoundedCornerShape(999.dp)
        WidgetBackgroundShape.SQUARE -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(background.cornerRadiusDp.dp)
    }
    val alignment = when (resolved.timeAlignment) {
        ClockTimeAlignment.START -> Alignment.Start
        ClockTimeAlignment.CENTER -> Alignment.CenterHorizontally
        ClockTimeAlignment.END -> Alignment.End
    }
    val textAlignment = when (resolved.timeAlignment) {
        ClockTimeAlignment.START -> TextAlign.Start
        ClockTimeAlignment.CENTER -> TextAlign.Center
        ClockTimeAlignment.END -> TextAlign.End
    }
    val dateTextAlignment = when (resolved.dateAlignment) {
        ClockDateAlignment.START -> TextAlign.Start
        ClockDateAlignment.CENTER -> TextAlign.Center
        ClockDateAlignment.END -> TextAlign.End
    }
    val timeColor = Color(resolved.timeColor)
    val timeFont = resolved.timeFontFamily.toComposeFontFamily()
    val dateFont = resolved.dateFontFamily.toComposeFontFamily()
    val dateItems = buildList {
        if (resolved.isWeekdayVisible) {
            add(PreviewDateItem(display.weekday, Color(resolved.weekdayColor)))
        }
        if (resolved.isDateVisible) {
            add(PreviewDateItem(display.date, Color(resolved.dateColor)))
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .offset(
                x = resolved.horizontalOffsetDp.dp,
                y = resolved.verticalOffsetDp.dp,
            )
            .clip(shape)
            .background(
                when (background.fill) {
                    WidgetBackgroundFill.TRANSPARENT -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    WidgetBackgroundFill.SOLID -> Brush.linearGradient(
                        listOf(backgroundColor.copy(alpha = background.solidOpacity * background.opacity), backgroundColor.copy(alpha = background.solidOpacity * background.opacity)),
                    )
                    WidgetBackgroundFill.LINEAR_GRADIENT -> Brush.linearGradient(
                        listOf(gradientStart.copy(alpha = background.opacity), gradientEnd.copy(alpha = background.opacity)),
                    )
                    WidgetBackgroundFill.RADIAL_GRADIENT -> Brush.radialGradient(
                        listOf(gradientStart.copy(alpha = background.opacity), gradientEnd.copy(alpha = background.opacity)),
                    )
                },
            )
            .drawWithCache {
                val highlight = Brush.radialGradient(
                    colors = listOf(
                        runCatching { Color(background.highlightColorHex.toColorInt()) }
                            .getOrDefault(Color.Transparent)
                            .copy(alpha = background.highlightOpacity.coerceIn(0f, 1f)),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.08f),
                    radius = size.maxDimension * 0.82f,
                )
                onDrawWithContent {
                    if (effects.glowEnabled && effects.glowStrength > 0f) {
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(
                                    runCatching { Color(effects.glowColorHex.toColorInt()) }
                                        .getOrDefault(Color.Transparent)
                                        .copy(alpha = effects.glowStrength.coerceIn(0f, 1f) * 0.45f),
                                    Color.Transparent,
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.58f),
                                radius = size.maxDimension * 0.72f,
                            ),
                        )
                    }
                    drawContent()
                    if (background.highlightEnabled) drawRect(highlight)
                }
            }
            .border(
                width = if (background.borderEnabled) background.borderWidthDp.dp else 0.dp,
                color = runCatching { Color(background.borderColorHex.toColorInt()) }
                    .getOrDefault(Color.White)
                    .copy(alpha = background.borderOpacity.coerceIn(0f, 1f)),
                    shape = shape,
            )
            .padding(resolved.contentPaddingDp.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.spacedBy(
                resolved.timeDateGapDp.dp,
                Alignment.CenterVertically,
            ),
        ) {
            if (configuration.date.placement == ClockDatePlacement.TOP) {
                PreviewDateRow(
                    dateItems = dateItems,
                    fontFamily = dateFont,
                    textAlignment = dateTextAlignment,
                    dateSizeSp = resolved.dateSizeSp,
                    dateWeight = resolved.dateFontWeight,
                )
            }
            Text(
                text = display.time,
                color = timeColor,
                fontFamily = timeFont,
                fontSize = resolved.timeSizeSp.sp,
                lineHeight = (resolved.timeSizeSp * 40f / ClockStyleResolver.BASE_TIME_SIZE_SP).sp,
                fontWeight = FontWeight(resolved.timeFontWeight.coerceIn(100, 900)),
                letterSpacing = resolved.letterSpacing.sp,
                textAlign = textAlignment,
            )
            if (configuration.date.placement == ClockDatePlacement.BOTTOM) {
                PreviewDateRow(
                    dateItems = dateItems,
                    fontFamily = dateFont,
                    textAlignment = dateTextAlignment,
                    dateSizeSp = resolved.dateSizeSp,
                    dateWeight = resolved.dateFontWeight,
                )
            }
        }
    }
}

@Composable
private fun PreviewDateRow(
    dateItems: List<PreviewDateItem>,
    fontFamily: FontFamily,
    textAlignment: TextAlign,
    dateSizeSp: Float,
    dateWeight: Int,
) {
    if (dateItems.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = when (textAlignment) {
                TextAlign.Start -> Arrangement.Start
                TextAlign.Center -> Arrangement.Center
                TextAlign.End -> Arrangement.End
                else -> Arrangement.Start
            },
        ) {
            dateItems.forEachIndexed { index, item ->
                Text(
                    modifier = Modifier.padding(
                        start = if (index > 0) 8.dp else 0.dp,
                    ),
                    text = item.text,
                    color = item.color,
                    fontFamily = fontFamily,
                    fontSize = dateSizeSp.sp,
                    lineHeight = dateSizeSp.sp,
                    fontWeight = FontWeight(
                        if (index == 0) dateWeight.coerceIn(100, 900) else FontWeight.Normal.weight,
                    ),
                    textAlign = textAlignment,
                )
            }
        }
    }
}

private data class PreviewDateItem(
    val text: String,
    val color: Color,
)

@Composable
fun FallbackPreview(definition: WidgetDefinition) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101017))
            .padding(AppSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${definition.category.label}\nPreview arriving next",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}


