package com.r2h_widget.widget.clock

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews
import com.r2h_widget.R
import com.r2h_widget.widget.common.BackgroundRenderRequest
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundRenderer
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.common.WidgetTapActions

object DigitalClockRemoteViewsRenderer {
    fun render(context: Context, configuration: DigitalClockConfiguration): RemoteViews {
        val resolved = ClockStyleResolver.resolve(configuration, WidgetSizeClass.MEDIUM)
        return renderBase(context, configuration, resolved).also { remoteViews ->
            val density = context.resources.displayMetrics.density
            remoteViews.setImageViewBitmap(
                R.id.widget_background,
                WidgetBackgroundRenderer.renderBlocking(
                    BackgroundRenderRequest(
                        widthPx = (220 * density).toInt(),
                        heightPx = (100 * density).toInt(),
                        density = density,
                        configuration = configuration.background,
                        effects = configuration.effects,
                    ),
                ),
            )
        }
    }

    suspend fun render(
        context: Context,
        configuration: DigitalClockConfiguration,
        appWidgetId: Int,
        productId: String = "digital-clock",
        options: android.os.Bundle? = null,
    ): RemoteViews {
        val density = context.resources.displayMetrics.density
        val widthDp = options?.getInt("appWidgetMinWidth", 220) ?: 220
        val heightDp = options?.getInt("appWidgetMinHeight", 100) ?: 100
        val sizeClass = WidgetSizeResolver.fromDp(widthDp, heightDp)
        val resolved = ClockStyleResolver.resolve(configuration, sizeClass)
        val remoteViews = renderBase(context, configuration, resolved)
        val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(1)
        val background = WidgetBackgroundRenderer.render(
            BackgroundRenderRequest(
                widthPx = widthPx,
                heightPx = heightPx,
                density = density,
                configuration = configuration.background,
                effects = configuration.effects,
            ),
        )
        remoteViews.setImageViewBitmap(R.id.widget_background, background)
        attachAction(context, remoteViews, configuration.action, appWidgetId, productId)
        return remoteViews
    }    private fun renderBase(
        context: Context,
        configuration: DigitalClockConfiguration,
        resolved: ResolvedClockStyle,
    ): RemoteViews {
        val remoteViews = RemoteViews(
            context.packageName,
            DigitalClockRemoteViewsLayouts.baseFor(resolved.timeFontFamily),
        )
        val timePattern = ClockFormatter.timePattern(configuration)
        val datePattern = ClockFormatter.datePattern(configuration.date)
        val weekdayPattern = ClockFormatter.weekdayPattern(configuration.date)
        val gapPx = resolved.timeDateGapDp * context.resources.displayMetrics.density
        remoteViews.setCharSequence(R.id.widget_clock, "setFormat12Hour", timePattern)
        remoteViews.setCharSequence(R.id.widget_clock, "setFormat24Hour", timePattern)
        configureDateRow(
            remoteViews = remoteViews,
            packageName = context.packageName,
            hostId = R.id.widget_date_content_top,
            gapId = R.id.widget_date_gap_top,
            gapPx = gapPx,
            rowId = R.id.widget_date_row_top,
            dateId = R.id.widget_date_top,
            weekdayId = R.id.widget_weekday_top,
            datePattern = datePattern,
            weekdayPattern = weekdayPattern,
            resolved = resolved,
            visible = configuration.date.placement == ClockDatePlacement.TOP,
        )
        configureDateRow(
            remoteViews = remoteViews,
            packageName = context.packageName,
            hostId = R.id.widget_date_content,
            gapId = R.id.widget_date_gap,
            gapPx = gapPx,
            rowId = R.id.widget_date_row,
            dateId = R.id.widget_date,
            weekdayId = R.id.widget_weekday,
            datePattern = datePattern,
            weekdayPattern = weekdayPattern,
            resolved = resolved,
            visible = configuration.date.placement == ClockDatePlacement.BOTTOM,
        )
        remoteViews.setTextColor(R.id.widget_clock, resolved.timeColor)
        remoteViews.setTextViewTextSize(
            R.id.widget_clock,
            TypedValue.COMPLEX_UNIT_SP,
            resolved.timeSizeSp,
        )
        remoteViews.setFloat(R.id.widget_clock, "setLetterSpacing", configuration.typography.letterSpacing)

        val timeGravity = when (resolved.timeAlignment) {
            ClockTimeAlignment.START -> Gravity.START
            ClockTimeAlignment.CENTER -> Gravity.CENTER_HORIZONTAL
            ClockTimeAlignment.END -> Gravity.END
        }
        val dateGravity = when (resolved.dateAlignment) {
            ClockDateAlignment.START -> Gravity.START
            ClockDateAlignment.CENTER -> Gravity.CENTER_HORIZONTAL
            ClockDateAlignment.END -> Gravity.END
        }
        remoteViews.setInt(R.id.widget_content, "setGravity", Gravity.CENTER_VERTICAL)
        remoteViews.setInt(R.id.widget_clock, "setGravity", timeGravity)
        remoteViews.setInt(R.id.widget_date_row, "setGravity", dateGravity)
        remoteViews.setInt(R.id.widget_date_row_top, "setGravity", dateGravity)
        remoteViews.setFloatDimen(
            R.id.widget_content,
            "setTranslationX",
            resolved.horizontalOffsetDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )
        remoteViews.setFloatDimen(
            R.id.widget_content,
            "setTranslationY",
            resolved.verticalOffsetDp,
            TypedValue.COMPLEX_UNIT_DIP,
        )
        val density = context.resources.displayMetrics.density
        val padding = (resolved.contentPaddingDp * density).toInt()
        remoteViews.setViewPadding(R.id.widget_content, padding, padding, padding, padding)
        return remoteViews
    }

    private fun configureDateRow(
        remoteViews: RemoteViews,
        packageName: String,
        hostId: Int,
        gapId: Int,
        gapPx: Float,
        rowId: Int,
        dateId: Int,
        weekdayId: Int,
        datePattern: String,
        weekdayPattern: String,
        resolved: ResolvedClockStyle,
        visible: Boolean,
    ) {
        val hasDate = resolved.isDateVisible
        val hasWeekday = resolved.isWeekdayVisible
        val rowVisible = visible && (hasDate || hasWeekday)
        remoteViews.removeAllViews(hostId)
        if (rowVisible) {
            remoteViews.addView(
                hostId,
                RemoteViews(
                    packageName,
                    DigitalClockRemoteViewsLayouts.dateFor(resolved.dateFontFamily),
                ),
            )
            remoteViews.setCharSequence(dateId, "setFormat12Hour", datePattern)
            remoteViews.setCharSequence(dateId, "setFormat24Hour", datePattern)
            remoteViews.setCharSequence(weekdayId, "setFormat12Hour", weekdayPattern)
            remoteViews.setCharSequence(weekdayId, "setFormat24Hour", weekdayPattern)
            remoteViews.setViewVisibility(dateId, if (hasDate) View.VISIBLE else View.GONE)
            remoteViews.setViewVisibility(weekdayId, if (hasWeekday) View.VISIBLE else View.GONE)
            remoteViews.setViewVisibility(
                R.id.widget_date_line,
                if (rowId == R.id.widget_date_row) View.VISIBLE else View.GONE,
            )
            remoteViews.setViewVisibility(
                R.id.widget_date_line_top,
                if (rowId == R.id.widget_date_row_top) View.VISIBLE else View.GONE,
            )
            remoteViews.setTextColor(dateId, resolved.dateColor)
            remoteViews.setTextColor(weekdayId, resolved.weekdayColor)
            remoteViews.setTextViewTextSize(dateId, TypedValue.COMPLEX_UNIT_SP, resolved.dateSizeSp)
            remoteViews.setTextViewTextSize(weekdayId, TypedValue.COMPLEX_UNIT_SP, resolved.dateSizeSp)
        }
        remoteViews.setViewVisibility(rowId, if (rowVisible) View.VISIBLE else View.GONE)
        remoteViews.setViewVisibility(gapId, if (rowVisible) View.VISIBLE else View.GONE)
        remoteViews.setViewLayoutHeight(
            gapId,
            if (rowVisible) gapPx else 0f,
            TypedValue.COMPLEX_UNIT_PX,
        )
    }

    private fun attachAction(
        context: Context,
        remoteViews: RemoteViews,
        action: WidgetActionConfiguration,
        appWidgetId: Int,
        productId: String,
    ) {
        val pendingIntent = WidgetTapActions.pendingIntentFor(context, action, appWidgetId, productId)
        if (pendingIntent != null) {
            remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        }
    }

}
