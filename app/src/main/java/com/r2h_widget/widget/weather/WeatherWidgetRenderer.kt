package com.r2h_widget.widget.weather

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import com.r2h_widget.R
import com.r2h_widget.widget.WidgetEditAction
import com.r2h_widget.widget.common.BackgroundRenderRequest
import com.r2h_widget.widget.common.WidgetBackgroundRenderer
import com.r2h_widget.widget.common.WidgetTapActions
import kotlin.math.roundToInt

/**
 * Builds the launcher RemoteViews for the Weather product.
 *
 * The renderer accepts either a live repository result or an explicit unavailable
 * result. The receiver uses [OpenMeteoWeatherRepository]; previews can use the
 * unavailable path without inventing device weather.
 */
object WeatherWidgetRenderer {

    fun unavailablePreview(
        context: Context,
        configuration: WeatherWidgetConfiguration = WeatherWidgetConfiguration(),
        options: Bundle? = null,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "weather",
    ): RemoteViews = renderSync(context, configuration, WeatherResult.Unavailable("Weather source is not connected"), options, appWidgetId, productId)

    suspend fun render(
        context: Context,
        configuration: WeatherWidgetConfiguration,
        repository: WeatherRepository = OpenMeteoWeatherRepository(context),
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "weather",
        options: Bundle? = null,
    ): RemoteViews = renderSync(context, configuration, repository.current(configuration), options, appWidgetId, productId)

    internal fun renderSync(
        context: Context,
        configuration: WeatherWidgetConfiguration,
        result: WeatherResult,
        options: Bundle?,
        appWidgetId: Int,
        productId: String,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_weather)
        val density = context.resources.displayMetrics.density
        val compactWidth = (options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) ?: 180) < 180

        views.setImageViewBitmap(
            R.id.weather_background,
            WidgetBackgroundRenderer.renderBlocking(
                BackgroundRenderRequest(
                    widthPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) ?: 180) * density).toInt().coerceAtLeast(1),
                    heightPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110) ?: 110) * density).toInt().coerceAtLeast(1),
                    density = density,
                    configuration = configuration.background,
                ),
            ),
        )

        when (result) {
            is WeatherResult.Available -> {
                bindReadout(views, configuration, compactWidth, result.temperature, result.condition, result.highLow, result.locationName)
            }
            is WeatherResult.Unavailable -> {
                bindReadout(
                    views,
                    configuration,
                    compactWidth,
                    if (configuration.showTemperature) "—" else "",
                    "Weather unavailable",
                    null,
                    result.reason,
                )
            }
        }

        val padding = (configuration.contentPaddingDp * density).roundToInt()
        views.setViewPadding(R.id.weather_content, padding, padding / 2, padding, padding / 2)
        val gravity = if (configuration.alignStart) Gravity.START or Gravity.CENTER_VERTICAL else Gravity.CENTER
        views.setInt(R.id.weather_content, "setGravity", gravity)

        val customAction = WidgetTapActions.pendingIntentFor(context, configuration.action, appWidgetId, productId)
        if (customAction != null) {
            views.setOnClickPendingIntent(R.id.weather_root, customAction)
        } else {
            WidgetEditAction.attach(context, views, R.id.weather_root, appWidgetId, productId)
        }
        return views
    }

    private fun bindReadout(
        views: RemoteViews,
        configuration: WeatherWidgetConfiguration,
        compactWidth: Boolean,
        temperature: String,
        condition: String,
        highLow: String?,
        location: String?,
    ) {
        views.setViewVisibility(R.id.weather_temperature, if (configuration.showTemperature) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.weather_temperature, temperature)
        views.setTextColor(R.id.weather_temperature, parseColor(configuration.temperatureColorHex))
        views.setTextViewTextSize(
            R.id.weather_temperature,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            (if (configuration.layout == WeatherWidgetLayout.COMPACT || compactWidth) 26f else 36f) * configuration.textScale,
        )

        val conditionText = condition.ifEmpty { "" }
        views.setViewVisibility(R.id.weather_condition, if (configuration.showCondition && conditionText.isNotEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.weather_condition, conditionText)
        views.setTextColor(R.id.weather_condition, parseColor(configuration.secondaryColorHex))
        views.setTextViewTextSize(
            R.id.weather_condition,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            (if (configuration.layout == WeatherWidgetLayout.COMPACT || compactWidth) 12f else 14f) * configuration.textScale,
        )

        val highLowText = highLow.orEmpty()
        views.setViewVisibility(R.id.weather_high_low, if (configuration.showHighLow && highLowText.isNotEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.weather_high_low, highLowText)
        views.setTextColor(R.id.weather_high_low, parseColor(configuration.secondaryColorHex))
        views.setTextViewTextSize(
            R.id.weather_high_low,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            12f * configuration.textScale,
        )

        val locationText = location.orEmpty().ifBlank { configuration.locationName.orEmpty() }
        views.setViewVisibility(R.id.weather_location, if (configuration.showLocation && locationText.isNotEmpty()) View.VISIBLE else View.GONE)
        views.setTextViewText(R.id.weather_location, locationText)
        views.setTextColor(R.id.weather_location, parseColor(configuration.secondaryColorHex))
        views.setTextViewTextSize(
            R.id.weather_location,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            11f * configuration.textScale,
        )

        views.setViewVisibility(R.id.weather_icon, if (configuration.showIcon) View.VISIBLE else View.GONE)
        views.setColorStateList(R.id.weather_icon, "setImageTintList", ColorStateList.valueOf(parseColor(configuration.iconTintHex)))
    }

    private fun parseColor(hex: String): Int = runCatching { Color.parseColor(hex) }.getOrDefault(Color.WHITE)
}

