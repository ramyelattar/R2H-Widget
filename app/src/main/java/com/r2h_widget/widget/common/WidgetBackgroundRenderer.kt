package com.r2h_widget.widget.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class BackgroundRenderRequest(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val configuration: WidgetBackgroundConfiguration,
    val effects: WidgetEffectsConfiguration = WidgetEffectsConfiguration(),
)

/** Renders the shared decorative widget surface into a bitmap for RemoteViews. */
object WidgetBackgroundRenderer {
    private const val MAX_CACHE_ENTRIES = 24
    private val cache = object : LinkedHashMap<CacheKey, Bitmap>(MAX_CACHE_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, Bitmap>?): Boolean = size > MAX_CACHE_ENTRIES
    }

    suspend fun render(request: BackgroundRenderRequest): Bitmap = withContext(Dispatchers.Default) {
        renderBlocking(request)
    }

    fun renderBlocking(request: BackgroundRenderRequest): Bitmap {
        val width = request.widthPx.coerceAtLeast(1)
        val height = request.heightPx.coerceAtLeast(1)
        val key = CacheKey(width, height, request.density, request.configuration, request.effects)
        synchronized(cache) {
            cache[key]?.let { return it }
        }
        val bitmap = createBitmap(width, height, request.density, request.configuration, request.effects)
        synchronized(cache) { cache[key] = bitmap }
        return bitmap
    }

    fun clearCache() {
        synchronized(cache) { cache.clear() }
    }

    private fun createBitmap(
        width: Int,
        height: Int,
        density: Float,
        configuration: WidgetBackgroundConfiguration,
        effects: WidgetEffectsConfiguration,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radius = radiusPx(configuration, width, height, density)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (configuration.opacity.coerceIn(0f, 1f) * 255).toInt()
        }

        when (configuration.fill) {
            WidgetBackgroundFill.TRANSPARENT -> Unit
            WidgetBackgroundFill.SOLID -> {
                fillPaint.color = parseColor(configuration.solidColorHex)
                fillPaint.alpha = (configuration.solidOpacity.coerceIn(0f, 1f) * configuration.opacity.coerceIn(0f, 1f) * 255).toInt()
                drawShape(canvas, bounds, radius, fillPaint, configuration.shape)
            }
            WidgetBackgroundFill.LINEAR_GRADIENT -> {
                fillPaint.shader = LinearGradient(
                    gradientX(width, configuration.gradientAngleDegrees, false),
                    gradientY(height, configuration.gradientAngleDegrees, false),
                    gradientX(width, configuration.gradientAngleDegrees, true),
                    gradientY(height, configuration.gradientAngleDegrees, true),
                    parseColor(configuration.gradientStartHex),
                    parseColor(configuration.gradientEndHex),
                    Shader.TileMode.CLAMP,
                )
                drawShape(canvas, bounds, radius, fillPaint, configuration.shape)
            }
            WidgetBackgroundFill.RADIAL_GRADIENT -> {
                fillPaint.shader = RadialGradient(
                    width * 0.18f,
                    height * 0.12f,
                    max(width, height) * 0.9f,
                    parseColor(configuration.gradientStartHex),
                    parseColor(configuration.gradientEndHex),
                    Shader.TileMode.CLAMP,
                )
                drawShape(canvas, bounds, radius, fillPaint, configuration.shape)
            }
        }

        if (configuration.highlightEnabled && configuration.highlightOpacity > 0f) {
            val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * 0.2f,
                    height * 0.05f,
                    max(width, height) * 0.78f,
                    withAlpha(parseColor(configuration.highlightColorHex), configuration.highlightOpacity),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP,
                )
            }
            drawShape(canvas, bounds, radius, highlightPaint, configuration.shape)
        }

        if (effects.glowEnabled && effects.glowStrength > 0f) {
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = withAlpha(parseColor(effects.glowColorHex), effects.glowStrength * 0.22f)
            }
            drawShape(canvas, bounds, radius, glowPaint, configuration.shape)
        }

        if (configuration.borderEnabled && configuration.borderWidthDp > 0f) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = configuration.borderWidthDp * density
                color = withAlpha(parseColor(configuration.borderColorHex), configuration.borderOpacity)
            }
            val inset = borderPaint.strokeWidth / 2f
            drawShape(
                canvas,
                RectF(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset),
                max(0f, radius - inset),
                borderPaint,
                configuration.shape,
            )
        }
        return bitmap
    }

    private fun drawShape(
        canvas: Canvas,
        bounds: RectF,
        radius: Float,
        paint: Paint,
        shape: WidgetBackgroundShape,
    ) {
        if (shape == WidgetBackgroundShape.SQUARE) {
            canvas.drawRect(bounds, paint)
        } else {
            canvas.drawRoundRect(bounds, radius, radius, paint)
        }
    }

    private fun radiusPx(configuration: WidgetBackgroundConfiguration, width: Int, height: Int, density: Float): Float = when (configuration.shape) {
        WidgetBackgroundShape.SQUARE -> 0f
        WidgetBackgroundShape.PILL -> min(width, height) / 2f
        WidgetBackgroundShape.ROUNDED,
        WidgetBackgroundShape.SOFT_RECTANGLE,
        -> configuration.cornerRadiusDp.coerceAtLeast(0f) * density
    }

    private fun gradientX(widthPx: Int, degrees: Float, end: Boolean): Float {
        val width = widthPx.toFloat()
        val radians = Math.toRadians(degrees.toDouble())
        return if (end) width / 2f + cos(radians).toFloat() * width else width / 2f - cos(radians).toFloat() * width
    }
    private fun gradientY(heightPx: Int, degrees: Float, end: Boolean): Float {
        val height = heightPx.toFloat()
        val radians = Math.toRadians(degrees.toDouble())
        return if (end) height / 2f + sin(radians).toFloat() * height else height / 2f - sin(radians).toFloat() * height
    }

    private fun parseColor(value: String): Int = runCatching { Color.parseColor(value) }.getOrDefault(Color.TRANSPARENT)

    private fun withAlpha(color: Int, alpha: Float): Int = Color.argb(
        (alpha.coerceIn(0f, 1f) * 255).toInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color),
    )

    private data class CacheKey(
        val width: Int,
        val height: Int,
        val density: Float,
        val configuration: WidgetBackgroundConfiguration,
        val effects: WidgetEffectsConfiguration,
    )
}
