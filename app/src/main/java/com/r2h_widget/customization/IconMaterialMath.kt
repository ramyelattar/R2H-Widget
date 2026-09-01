package com.r2h_widget.customization

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max

/**
 * Compact, deterministic source-color analysis shared by the runtime
 * material recipes. It deliberately describes the source instead of trying
 * to segment opaque launcher artwork into semantic logo parts.
 */
data class IconColorProfile(
    val averageColor: Int,
    val luminance: Float,
    val saturation: Float,
    val alphaOccupancy: Float,
)

object IconMaterialMath {
    fun profile(bitmap: Bitmap, alphaThreshold: Int = IconForegroundNormalizer.DEFAULT_ALPHA_THRESHOLD): IconColorProfile {
        val step = max(1, bitmap.width / 24)
        var red = 0L
        var green = 0L
        var blue = 0L
        var opaqueSamples = 0
        var visibleSamples = 0

        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val color = bitmap.getPixel(x, y)
                val alpha = Color.alpha(color)
                if (alpha > alphaThreshold) {
                    visibleSamples++
                    red += Color.red(color)
                    green += Color.green(color)
                    blue += Color.blue(color)
                    if (alpha > 220) opaqueSamples++
                }
                x += step
            }
            y += step
        }

        if (visibleSamples == 0) {
            return IconColorProfile(
                averageColor = Color.rgb(104, 141, 232),
                luminance = 0.52f,
                saturation = 0.55f,
                alphaOccupancy = 0f,
            )
        }

        val average = Color.rgb(
            (red / visibleSamples).toInt().coerceIn(0, 255),
            (green / visibleSamples).toInt().coerceIn(0, 255),
            (blue / visibleSamples).toInt().coerceIn(0, 255),
        )
        val maximum = max(Color.red(average), max(Color.green(average), Color.blue(average)))
        val minimum = minOf(Color.red(average), Color.green(average), Color.blue(average))
        return IconColorProfile(
            averageColor = average,
            luminance = luminance(average),
            saturation = if (maximum == 0) 0f else (maximum - minimum) / maximum.toFloat(),
            alphaOccupancy = opaqueSamples / visibleSamples.toFloat(),
        )
    }

    fun luminance(color: Int): Float = (
        Color.red(color) * 0.2126f +
            Color.green(color) * 0.7152f +
            Color.blue(color) * 0.0722f
        ) / 255f

    fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color),
    )

    fun blend(from: Int, to: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(from) + (Color.red(to) - Color.red(from)) * t).toInt().coerceIn(0, 255),
            (Color.green(from) + (Color.green(to) - Color.green(from)) * t).toInt().coerceIn(0, 255),
            (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t).toInt().coerceIn(0, 255),
        )
    }
}
