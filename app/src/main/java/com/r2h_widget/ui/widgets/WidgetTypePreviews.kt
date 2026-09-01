package com.r2h_widget.ui.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.analog.AnalogClockPainter
import com.r2h_widget.widget.analog.AnalogClockRemoteViewsRenderer
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetLayout
import com.r2h_widget.widget.battery.BatteryWidgetState
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetBackgroundShape
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetLayout
import kotlinx.coroutines.delay

/**
 * Compose previews for Analog Clock, Battery, Weather, and Search. Each preview reads
 * the same typed configuration as its launcher renderer, so the studio shows exactly
 * what lands on the home screen.
 */

internal fun studioColor(hex: String, fallback: Long): Color =
    runCatching { Color(hex.toColorInt()) }.getOrDefault(Color(fallback))

internal fun Modifier.widgetPreviewSurface(background: WidgetBackgroundConfiguration): Modifier {
    val shape = when (background.shape) {
        WidgetBackgroundShape.PILL -> RoundedCornerShape(999.dp)
        WidgetBackgroundShape.SQUARE -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(background.cornerRadiusDp.dp)
    }
    val solid = studioColor(background.solidColorHex, 0xFF15151F)
    val start = studioColor(background.gradientStartHex, 0xFF171229)
    val end = studioColor(background.gradientEndHex, 0xFF090812)
    val brush = when (background.fill) {
        WidgetBackgroundFill.TRANSPARENT -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
        WidgetBackgroundFill.SOLID -> Brush.linearGradient(
            listOf(
                solid.copy(alpha = background.solidOpacity * background.opacity),
                solid.copy(alpha = background.solidOpacity * background.opacity),
            ),
        )
        WidgetBackgroundFill.LINEAR_GRADIENT -> Brush.linearGradient(
            listOf(start.copy(alpha = background.opacity), end.copy(alpha = background.opacity)),
        )
        WidgetBackgroundFill.RADIAL_GRADIENT -> Brush.radialGradient(
            listOf(start.copy(alpha = background.opacity), end.copy(alpha = background.opacity)),
        )
    }
    val borderColor = studioColor(background.borderColorHex, 0xFFFFFFFF)
    return this
        .clip(shape)
        .background(brush)
        .border(
            width = if (background.borderEnabled) background.borderWidthDp.dp else 0.dp,
            color = borderColor.copy(alpha = if (background.borderEnabled) background.borderOpacity else 0f),
            shape = shape,
        )
}

@Composable
fun AnalogClockPreview(
    modifier: Modifier = Modifier,
    configuration: AnalogClockConfiguration = AnalogClockConfiguration(),
    live: Boolean = true,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    if (live) {
        LaunchedEffect(configuration.showSecondHand) {
            while (true) {
                now = System.currentTimeMillis()
                delay(if (configuration.showSecondHand) 1_000L else 30_000L)
            }
        }
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }.toInt().coerceAtLeast(1)
        val heightPx = with(density) { maxHeight.toPx() }.toInt().coerceAtLeast(1)
        val dateText = remember(configuration.date, now) {
            AnalogClockRemoteViewsRenderer.dateTextFor(configuration, now)
        }
        val dialBitmap = remember(configuration, widthPx, heightPx, now) {
            Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888).also { bitmap ->
                AnalogClockPainter.draw(
                    canvas = Canvas(bitmap),
                    widthPx = widthPx,
                    heightPx = heightPx,
                    density = density.density,
                    configuration = configuration,
                    timeMillis = now,
                    dateText = dateText,
                )
            }
        }
        Image(
            bitmap = dialBitmap.asImageBitmap(),
            contentDescription = "Analog clock preview",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun BatteryPreview(
    modifier: Modifier = Modifier,
    configuration: BatteryWidgetConfiguration = BatteryWidgetConfiguration(),
    state: BatteryWidgetState = BatteryWidgetState(73, false, com.r2h_widget.widget.battery.BatteryPlugSource.UNKNOWN, false),
) {
    val indicatorColor = studioColor(configuration.indicatorColorHex(state), 0xFF34D399)
    val percentColor = studioColor(configuration.percentColorHex, 0xFFF4F2FA)
    val statusColor = if (state.isCharging) indicatorColor else studioColor(configuration.statusColorHex, 0xFFA7A4B5)
    val horizontalAlignment = if (configuration.alignStart) Alignment.Start else Alignment.CenterHorizontally

    Box(
        modifier = modifier
            .fillMaxSize()
            .widgetPreviewSurface(configuration.background)
            .padding(configuration.contentPaddingDp.dp),
    ) {
        when (configuration.layout) {
            BatteryWidgetLayout.STACKED -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (configuration.showIcon && state.isCharging) {
                        BoltIcon(indicatorColor)
                    }
                    if (configuration.showPercentage) {
                        Text(
                            "${state.levelPercent}%",
                            color = percentColor,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = (34 * configuration.textScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                LevelBar(configuration, state, indicatorColor)
                StatusText(configuration, state, statusColor)
            }

            BatteryWidgetLayout.HORIZONTAL -> Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (configuration.showIcon && state.isCharging) {
                        BoltIcon(indicatorColor)
                    }
                    if (configuration.showPercentage) {
                        Text(
                            "${state.levelPercent}%",
                            color = percentColor,
                            fontSize = (24 * configuration.textScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    LevelBar(configuration, state, indicatorColor)
                }
                StatusText(configuration, state, statusColor, inline = true)
            }

            BatteryWidgetLayout.COMPACT -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = Arrangement.Center,
            ) {
                if (configuration.showPercentage) {
                    Text(
                        "${state.levelPercent}%",
                        color = percentColor,
                        fontSize = (26 * configuration.textScale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LevelBar(configuration, state, indicatorColor, height = 4.dp)
            }
        }
    }
}

@Composable
private fun BoltIcon(color: Color) {
    Text("⚡", color = color, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun LevelBar(
    configuration: BatteryWidgetConfiguration,
    state: BatteryWidgetState,
    indicatorColor: Color,
    height: androidx.compose.ui.unit.Dp = 6.dp,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.14f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(state.levelPercent / 100f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(indicatorColor),
        )
    }
}

@Composable
private fun StatusText(
    configuration: BatteryWidgetConfiguration,
    state: BatteryWidgetState,
    color: Color,
    inline: Boolean = false,
) {
    val status = com.r2h_widget.widget.battery.BatteryWidgetRenderer.statusText(configuration, state)
    if (status.isEmpty()) {
        if (!inline) Spacer(Modifier.height(0.dp))
        return
    }
    Text(
        status,
        color = color,
        style = MaterialTheme.typography.bodySmall,
        fontSize = (12 * configuration.textScale).sp,
        modifier = if (inline) Modifier else Modifier.padding(top = 6.dp),
    )
}

@Composable
fun WeatherPreview(
    modifier: Modifier = Modifier,
    configuration: WeatherWidgetConfiguration = WeatherWidgetConfiguration(),
) {
    val temperatureColor = studioColor(configuration.temperatureColorHex, 0xFFF4F2FA)
    val secondaryColor = studioColor(configuration.secondaryColorHex, 0xFFA7A4B5)
    val horizontalAlignment = if (configuration.alignStart) Alignment.Start else Alignment.CenterHorizontally

    // The gallery has no live snapshot input, so it shows the same honest
    // unavailable state used when the launcher has no weather data yet.
    Box(
        modifier = modifier
            .fillMaxSize()
            .widgetPreviewSurface(configuration.background)
            .padding(configuration.contentPaddingDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (configuration.layout == WeatherWidgetLayout.COMPACT) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (configuration.showIcon) WeatherGlyph(configuration)
                if (configuration.showTemperature) {
                    Text(
                        "—",
                        color = temperatureColor,
                        fontSize = (26 * configuration.textScale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (configuration.showCondition) {
                    Text(
                        "Weather unavailable",
                        color = secondaryColor,
                        fontSize = (12 * configuration.textScale).sp,
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (configuration.showIcon) WeatherGlyph(configuration)
                    if (configuration.showTemperature) {
                        Text(
                            "—",
                            color = temperatureColor,
                            fontSize = (36 * configuration.textScale).sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                if (configuration.showCondition) {
                    Text(
                        "Weather unavailable",
                        color = secondaryColor,
                        fontSize = (14 * configuration.textScale).sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (configuration.showLocation) {
                    Text(
                        configuration.locationName ?: "Connect a weather source",
                        color = secondaryColor.copy(alpha = 0.8f),
                        fontSize = (11 * configuration.textScale).sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherGlyph(configuration: WeatherWidgetConfiguration) {
    val tint = studioColor(configuration.iconTintHex, 0xFF7DD3FC)
    // Simple cloud outline drawn with two circles and a rounded base.
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.size(24.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(tint)
                .align(Alignment.TopStart),
        )
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(tint)
                .align(Alignment.TopEnd),
        )
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(tint)
                .align(Alignment.BottomCenter),
        )
    }
}

@Composable
fun SearchPreview(
    modifier: Modifier = Modifier,
    configuration: SearchWidgetConfiguration = SearchWidgetConfiguration(),
) {
    val hintColor = studioColor(configuration.hintColorHex, 0xFFA7A4B5)
    val iconColor = studioColor(configuration.iconColorHex, 0xFFB69CFF)

    Row(
        modifier = modifier
            .fillMaxSize()
            .widgetPreviewSurface(configuration.background)
            .padding(horizontal = configuration.contentPaddingDp.dp)
            .padding(vertical = configuration.contentPaddingDp.dp / 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (configuration.showSearchIcon) {
            SearchIconVector(iconColor)
        }
        Text(
            configuration.hintText.ifBlank { "Search" },
            color = hintColor,
            fontSize = (15 * configuration.textScale).sp,
            modifier = Modifier.weight(1f),
        )
        if (configuration.showVoiceIcon) {
            MicIconVector(iconColor)
        }
    }
}

@Composable
private fun SearchIconVector(color: Color) {
    // Magnifier: ring + handle, matching ic_widget_search.
    Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .border(2.dp, color, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(2.dp)
                .background(color)
                .align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun MicIconVector(color: Color) {
    // Microphone: capsule + base stroke, matching ic_widget_mic.
    Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(2.dp)
                .background(color)
                .align(Alignment.BottomCenter),
        )
    }
}
