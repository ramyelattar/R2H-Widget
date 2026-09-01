package com.r2h_widget.widget.calendar

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.r2h_widget.R
import com.r2h_widget.widget.WidgetEditAction

object CalendarWidgetRenderer {
    fun render(
        context: android.content.Context,
        configuration: CalendarWidgetConfiguration,
        options: Bundle? = null,
        appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
        productId: String = "calendar-foundation",
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_calendar)
        val compact = (options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 110) ?: 110) < 180
        views.setViewVisibility(R.id.calendar_weekday, if (configuration.showWeekday) View.VISIBLE else View.GONE)
        views.setViewVisibility(R.id.calendar_month, if (configuration.showMonth) View.VISIBLE else View.GONE)
        views.setCharSequence(R.id.calendar_weekday, "setFormat12Hour", "EEEE")
        views.setCharSequence(R.id.calendar_weekday, "setFormat24Hour", "EEEE")
        views.setCharSequence(R.id.calendar_month, "setFormat12Hour", "d MMM yyyy")
        views.setCharSequence(R.id.calendar_month, "setFormat24Hour", "d MMM yyyy")
        views.setTextViewTextSize(
            R.id.calendar_weekday,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            if (compact) 14f else 18f,
        )
        views.setTextViewTextSize(
            R.id.calendar_month,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            if (compact) 22f else 30f,
        )
        WidgetEditAction.attach(context, views, R.id.calendar_root, appWidgetId, productId)
        return views
    }
}
