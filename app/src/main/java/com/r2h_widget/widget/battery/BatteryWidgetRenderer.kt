package com.r2h_widget.widget.battery

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
 * Builds the launcher RemoteViews for the Battery product. Layout, colors, and text
 * all follow the typed configuration; every value shown comes from the real
 * [BatteryWidgetState], never a placeholder.
 */
object BatteryWidgetRenderer {

    fun render(
        context: Context,
        configuration: BatteryWidgetConfiguration,
        state: BatteryWidgetState = BatteryStateReader.read(context),
        options: Bundle? = null,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "battery",
    ): RemoteViews {
        val compactWidth = (options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110) ?: 110) < 180
        val layoutRes = when (configuration.layout) {
            BatteryWidgetLayout.STACKED -> R.layout.widget_battery
            BatteryWidgetLayout.HORIZONTAL -> R.layout.widget_battery_horizontal
            BatteryWidgetLayout.COMPACT -> R.layout.widget_battery_compact
        }
        val views = RemoteViews(context.packageName, layoutRes)
        val density = context.resources.displayMetrics.density
        val indicatorColor = parseColor(configuration.indicatorColorHex(state))

        views.setImageViewBitmap(
            R.id.battery_background,
            WidgetBackgroundRenderer.renderBlocking(
                BackgroundRenderRequest(
                    widthPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180) ?: 180) * density).toInt().coerceAtLeast(1),
                    heightPx = ((options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70) ?: 70) * density).toInt().coerceAtLeast(1),
                    density = density,
                    configuration = configuration.background,
                ),
            ),
        )
        views.setProgressBar(R.id.battery_progress, 100, state.levelPercent, false)
        views.setColorStateList(R.id.battery_progress, "setProgressTintList", ColorStateList.valueOf(indicatorColor))

        val percentVisible = configuration.showPercentage
        views.setViewVisibility(R.id.battery_percent, if (percentVisible) View.VISIBLE else View.GONE)
        if (percentVisible) {
            views.setTextViewText(R.id.battery_percent, "${state.levelPercent}%")
            views.setTextColor(R.id.battery_percent, parseColor(configuration.percentColorHex))
            views.setTextViewTextSize(
                R.id.battery_percent,
                android.util.TypedValue.COMPLEX_UNIT_SP,
                basePercentSizeSp(configuration.layout, compactWidth) * configuration.textScale,
            )
        }

        if (configuration.layout != BatteryWidgetLayout.COMPACT) {
            val status = statusText(configuration, state)
            val statusViewVisible = status.isNotEmpty()
            views.setViewVisibility(R.id.battery_status, if (statusViewVisible) View.VISIBLE else View.GONE)
            if (statusViewVisible) {
                views.setTextViewText(R.id.battery_status, status)
                views.setTextColor(
                    R.id.battery_status,
                    if (state.isCharging) indicatorColor else parseColor(configuration.statusColorHex),
                )
                views.setTextViewTextSize(
                    R.id.battery_status,
                    android.util.TypedValue.COMPLEX_UNIT_SP,
                    (if (compactWidth) 11f else 13f) * configuration.textScale,
                )
            }
            val iconVisible = configuration.showIcon && state.isCharging
            views.setViewVisibility(R.id.battery_icon, if (iconVisible) View.VISIBLE else View.GONE)
            if (iconVisible) {
                views.setColorStateList(R.id.battery_icon, "setImageTintList", ColorStateList.valueOf(indicatorColor))
            }
        }

        val padding = (configuration.contentPaddingDp * density).roundToInt()
        views.setViewPadding(R.id.battery_content, padding, padding / 2, padding, padding / 2)
        val gravity = if (configuration.alignStart) Gravity.START or Gravity.CENTER_VERTICAL else Gravity.CENTER
        views.setInt(R.id.battery_content, "setGravity", gravity)

        val customAction = WidgetTapActions.pendingIntentFor(context, configuration.action, appWidgetId, productId)
        if (customAction != null) {
            views.setOnClickPendingIntent(R.id.battery_root, customAction)
        } else {
            WidgetEditAction.attach(context, views, R.id.battery_root, appWidgetId, productId)
        }
        return views
    }

    internal fun statusText(configuration: BatteryWidgetConfiguration, state: BatteryWidgetState): String = buildString {
        if (configuration.showChargingState) {
            append(if (state.isCharging) "Charging" else "On battery")
        }
        if (configuration.showPlugSource && state.plugSource != BatteryPlugSource.UNKNOWN) {
            if (isNotEmpty()) append(" · ")
            append(state.plugSource.name.lowercase().replaceFirstChar { it.uppercase() })
        }
        if (state.isLow && !state.isCharging) {
            if (isNotEmpty()) append(" · ")
            append("Low")
        }
    }

    private fun basePercentSizeSp(layout: BatteryWidgetLayout, compactWidth: Boolean): Float = when (layout) {
        BatteryWidgetLayout.STACKED -> if (compactWidth) 28f else 36f
        BatteryWidgetLayout.HORIZONTAL -> if (compactWidth) 20f else 26f
        BatteryWidgetLayout.COMPACT -> if (compactWidth) 22f else 30f
    }

    private fun parseColor(hex: String): Int = runCatching { Color.parseColor(hex) }.getOrDefault(Color.WHITE)
}
