package com.r2h_widget.widget.analog

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.r2h_widget.widget.clock.ClockSystemFontResolver
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the analog dial — plate, markers, numerals, hands, cap, and date — onto an
 * android.graphics.Canvas. The Compose preview and the launcher RemoteViews renderer
 * both call this painter so preview and home-screen rendering stay identical.
 */
object AnalogClockPainter {

    data class HandAngles(val hourDegrees: Float, val minuteDegrees: Float, val secondDegrees: Float)

    /** Exact hand positions for [timeMillis]; 0° points to 12, growing clockwise. */
    fun handAngles(timeMillis: Long): HandAngles {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val hour = calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE) / 60f + calendar.get(Calendar.SECOND) / 3600f
        val minute = calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) / 60f + calendar.get(Calendar.MILLISECOND) / 60_000f
        val second = calendar.get(Calendar.SECOND) + calendar.get(Calendar.MILLISECOND) / 1000f
        return HandAngles(
            hourDegrees = (hour % 12f) * HOUR_DEGREES,
            minuteDegrees = (minute % 60f) * MINUTE_DEGREES,
            secondDegrees = (second % 60f) * MINUTE_DEGREES,
        )
    }

    fun draw(
        canvas: Canvas,
        widthPx: Int,
        heightPx: Int,
        density: Float,
        configuration: AnalogClockConfiguration,
        timeMillis: Long,
        dateText: String?,
    ) {
        val width = widthPx.toFloat().coerceAtLeast(1f)
        val height = heightPx.toFloat().coerceAtLeast(1f)
        val dateTextHeight = if (dateText != null) dateTextSizePx(density, configuration) * DATE_BLOCK_FACTOR else 0f
        val dialTop = if (configuration.date.enabled && configuration.date.placement == com.r2h_widget.widget.clock.ClockDatePlacement.TOP) dateTextHeight else 0f
        val dialBottom = if (configuration.date.enabled && configuration.date.placement == com.r2h_widget.widget.clock.ClockDatePlacement.BOTTOM) height - dateTextHeight else height

        val radius = (minOf(width, dialBottom - dialTop) / 2f) * (0.86f * configuration.contentScale.coerceIn(0.6f, 1.15f))
        val cx = width / 2f
        val cy = dialTop + (dialBottom - dialTop) / 2f
        if (radius <= 1f) return

        if (configuration.dialFillEnabled) {
            val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = parseColor(configuration.dialColorHex, DEFAULT_DIAL_COLOR)
            }
            canvas.drawCircle(cx, cy, radius, dialPaint)
        }

        drawMarkers(canvas, cx, cy, radius, density, configuration)
        drawNumerals(canvas, cx, cy, radius, density, configuration)
        drawHands(canvas, cx, cy, radius, density, configuration, timeMillis)
        drawCap(canvas, cx, cy, radius, density, configuration)
        drawDate(canvas, width, height, density, configuration, dateText, dateTextHeight)
    }

    private fun drawMarkers(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        density: Float,
        configuration: AnalogClockConfiguration,
    ) {
        if (!configuration.showHourMarkers && !configuration.showMinuteMarkers) return
        val markerColor = parseColor(configuration.markerColorHex, DEFAULT_MARKER_COLOR)
        val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = markerColor
            strokeWidth = when (configuration.markerStyle) {
                AnalogMarkerStyle.DOTS -> 0f
                AnalogMarkerStyle.LINES -> 1.4f * density
            }
            style = if (configuration.markerStyle == AnalogMarkerStyle.DOTS) Paint.Style.FILL else Paint.Style.STROKE
        }
        val hourPaint = Paint(minutePaint).apply {
            strokeWidth = when (configuration.markerStyle) {
                AnalogMarkerStyle.DOTS -> 0f
                AnalogMarkerStyle.LINES -> 2.6f * density
            }
        }

        for (index in 0 until MARKER_COUNT) {
            val isHour = index % 5 == 0
            if (isHour && !configuration.showHourMarkers) continue
            if (!isHour && !configuration.showMinuteMarkers) continue
            val angle = Math.toRadians((index * (360f / MARKER_COUNT)).toDouble())
            val outer = radius * 0.94f
            val inner = radius * if (isHour) 0.83f else 0.89f
            val xOuter = cx + (sin(angle) * outer).toFloat()
            val yOuter = cy - (cos(angle) * outer).toFloat()
            when (configuration.markerStyle) {
                AnalogMarkerStyle.DOTS -> {
                    val dotRadius = (if (isHour) 0.026f else 0.013f) * radius
                    canvas.drawCircle(xOuter, yOuter, dotRadius, minutePaint)
                }
                AnalogMarkerStyle.LINES -> {
                    val xInner = cx + (sin(angle) * inner).toFloat()
                    val yInner = cy - (cos(angle) * inner).toFloat()
                    canvas.drawLine(xInner, yInner, xOuter, yOuter, if (isHour) hourPaint else minutePaint)
                }
            }
        }
    }

    private fun drawNumerals(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        density: Float,
        configuration: AnalogClockConfiguration,
    ) {
        if (configuration.numberStyle == AnalogNumberStyle.NONE) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(configuration.numberColorHex, DEFAULT_NUMERAL_COLOR)
            textSize = radius * 0.17f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val step = if (configuration.numberStyle == AnalogNumberStyle.ALL) 1 else 3
        var index = 0
        while (index < 12) {
            val numeral = if (index == 0) 12 else index
            val angle = Math.toRadians((index * 30f).toDouble())
            val position = radius * 0.70f
            val x = cx + (sin(angle) * position).toFloat()
            val y = cy - (cos(angle) * position).toFloat() - (paint.ascent() + paint.descent()) / 2f
            canvas.drawText(numeral.toString(), x, y, paint)
            index += step
        }
    }

    private fun drawHands(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        density: Float,
        configuration: AnalogClockConfiguration,
        timeMillis: Long,
    ) {
        val angles = handAngles(timeMillis)
        if (configuration.showHourHand) {
            drawHand(canvas, cx, cy, angles.hourDegrees, radius * 0.52f, radius * 0.045f, configuration.hourHandColorHex)
        }
        if (configuration.showMinuteHand) {
            drawHand(canvas, cx, cy, angles.minuteDegrees, radius * 0.76f, radius * 0.032f, configuration.minuteHandColorHex)
        }
        if (configuration.showSecondHand) {
            drawHand(canvas, cx, cy, angles.secondDegrees, radius * 0.80f, radius * 0.014f, configuration.secondHandColorHex, tailFactor = 0.16f)
        }
    }

    private fun drawHand(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        degrees: Float,
        length: Float,
        width: Float,
        colorHex: String,
        tailFactor: Float = 0f,
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(colorHex, DEFAULT_HAND_COLOR)
            strokeWidth = width
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        val angle = Math.toRadians(degrees.toDouble())
        val sin = sin(angle).toFloat()
        val cos = cos(angle).toFloat()
        canvas.drawLine(
            cx - sin * length * tailFactor,
            cy + cos * length * tailFactor,
            cx + sin * length,
            cy - cos * length,
            paint,
        )
    }

    private fun drawCap(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        density: Float,
        configuration: AnalogClockConfiguration,
    ) {
        val capRadius = radius * 0.055f
        when (configuration.capStyle) {
            AnalogCapStyle.DOT -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = parseColor(configuration.capColorHex, DEFAULT_ACCENT_COLOR)
                }
                canvas.drawCircle(cx, cy, capRadius, paint)
            }
            AnalogCapStyle.RING -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = parseColor(configuration.capColorHex, DEFAULT_ACCENT_COLOR)
                    style = Paint.Style.STROKE
                    strokeWidth = capRadius * 0.45f
                }
                canvas.drawCircle(cx, cy, capRadius, paint)
            }
        }
    }

    private fun drawDate(
        canvas: Canvas,
        width: Float,
        height: Float,
        density: Float,
        configuration: AnalogClockConfiguration,
        dateText: String?,
        dateBlockHeight: Float,
    ) {
        if (dateText == null || dateBlockHeight <= 0f) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColor(configuration.dateColorHex, DEFAULT_DATE_COLOR)
            textSize = dateTextSizePx(density, configuration)
            typeface = Typeface.create(ClockSystemFontResolver.familyName(configuration.dateFontFamily), Typeface.NORMAL)
        }
        val textWidth = paint.measureText(dateText)
        val padding = configuration.contentPaddingDp * density
        val baselineOffset = -(paint.ascent() + paint.descent()) / 2f
        val x = when (configuration.dateAlignment) {
            com.r2h_widget.widget.clock.ClockDateAlignment.START -> padding
            com.r2h_widget.widget.clock.ClockDateAlignment.CENTER -> (width - textWidth) / 2f
            com.r2h_widget.widget.clock.ClockDateAlignment.END -> width - textWidth - padding
        }
        paint.textAlign = Paint.Align.LEFT
        val centerY = if (configuration.date.placement == com.r2h_widget.widget.clock.ClockDatePlacement.TOP) {
            dateBlockHeight / 2f + baselineOffset
        } else {
            height - dateBlockHeight / 2f + baselineOffset
        }
        canvas.drawText(dateText, x, centerY, paint)
    }

    private fun dateTextSizePx(density: Float, configuration: AnalogClockConfiguration): Float = 12f * density * configuration.contentScale

    private fun parseColor(value: String, fallback: Int): Int =
        runCatching { Color.parseColor(value) }.getOrDefault(fallback)

    private const val HOUR_DEGREES = 30f
    private const val MINUTE_DEGREES = 6f
    private const val MARKER_COUNT = 60
    private const val DATE_BLOCK_FACTOR = 1.9f
    private val DEFAULT_DIAL_COLOR = 0xFF141024.toInt()
    private val DEFAULT_MARKER_COLOR = 0xFF8E8A9E.toInt()
    private val DEFAULT_NUMERAL_COLOR = 0xFFF4F2FA.toInt()
    private val DEFAULT_HAND_COLOR = 0xFFF4F2FA.toInt()
    private val DEFAULT_DATE_COLOR = 0xFFA7A4B5.toInt()
    private val DEFAULT_ACCENT_COLOR = 0xFF38BDF8.toInt()
}
