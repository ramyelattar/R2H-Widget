package com.r2h_widget.widget.analog

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.RemoteViews
import com.r2h_widget.R
import com.r2h_widget.widget.clock.ClockFormatter
import com.r2h_widget.widget.common.BackgroundRenderRequest
import com.r2h_widget.widget.common.WidgetBackgroundRenderer
import com.r2h_widget.widget.common.WidgetTapActions
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Builds the launcher RemoteViews for the Analog Clock product: the shared surface
 * bitmap below and the painted dial above. The dial is a bitmap drawn by
 * [AnalogClockPainter], the same engine the Compose preview uses.
 */
object AnalogClockRemoteViewsRenderer {

    fun render(
        context: Context,
        configuration: AnalogClockConfiguration,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "analog-clock",
        options: Bundle? = null,
        timeMillis: Long = System.currentTimeMillis(),
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        val widthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) ?: 180
        val heightDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180) ?: 180
        val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(1)

        val views = RemoteViews(context.packageName, R.layout.widget_analog_clock)
        views.setImageViewBitmap(
            R.id.analog_widget_background,
            WidgetBackgroundRenderer.renderBlocking(
                BackgroundRenderRequest(
                    widthPx = widthPx,
                    heightPx = heightPx,
                    density = density,
                    configuration = configuration.background,
                    effects = configuration.effects,
                ),
            ),
        )
        views.setImageViewBitmap(
            R.id.analog_dial,
            renderDialBitmap(configuration, widthPx, heightPx, density, timeMillis),
        )
        val pendingIntent = WidgetTapActions.pendingIntentFor(context, configuration.action, appWidgetId, productId)
        if (pendingIntent != null) {
            views.setOnClickPendingIntent(R.id.analog_widget_root, pendingIntent)
        }
        return views
    }

    private fun renderDialBitmap(
        configuration: AnalogClockConfiguration,
        widthPx: Int,
        heightPx: Int,
        density: Float,
        timeMillis: Long,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        AnalogClockPainter.draw(
            canvas = Canvas(bitmap),
            widthPx = widthPx,
            heightPx = heightPx,
            density = density,
            configuration = configuration,
            timeMillis = timeMillis,
            dateText = dateTextFor(configuration, timeMillis),
        )
        return bitmap
    }

    internal fun dateTextFor(configuration: AnalogClockConfiguration, timeMillis: Long): String? {
        if (!configuration.date.enabled) return null
        val zone = ZoneId.systemDefault()
        val zoned = Instant.ofEpochMilli(timeMillis).atZone(zone)
        val locale = Locale.getDefault()
        val date = DateTimeFormatter.ofPattern(ClockFormatter.datePattern(configuration.date), locale).format(zoned)
        return when (configuration.date.weekdayStyle) {
            com.r2h_widget.widget.clock.ClockWeekdayStyle.HIDDEN -> date
            else -> date + "  " + DateTimeFormatter
                .ofPattern(ClockFormatter.weekdayPattern(configuration.date), locale)
                .format(zoned)
        }
    }
}
