package com.r2h_widget.widget.common

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import com.r2h_widget.MainActivity
import com.r2h_widget.widget.clock.EXTRA_EDIT_APP_WIDGET_ID
import com.r2h_widget.widget.clock.EXTRA_EDIT_PRODUCT_ID

/** Builds the tap PendingIntent shared by every widget renderer. */
object WidgetTapActions {

    /**
     * Returns the PendingIntent for [action], or `null` when the action is disabled or
     * its target cannot resolve. `OPEN_APP` deep-links into this widget's studio.
     */
    fun pendingIntentFor(
        context: Context,
        action: WidgetActionConfiguration,
        appWidgetId: Int,
        productId: String,
    ): PendingIntent? {
        if (action.type == WidgetActionType.NONE || appWidgetId < 0) return null
        val intent = when (action.type) {
            WidgetActionType.NONE -> return null
            WidgetActionType.OPEN_APP -> Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_EDIT_APP_WIDGET_ID, appWidgetId)
                .putExtra(EXTRA_EDIT_PRODUCT_ID, productId)
            WidgetActionType.OPEN_ALARM -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            WidgetActionType.OPEN_CALENDAR -> Intent(Intent.ACTION_VIEW).setData(CalendarContract.CONTENT_URI)
            WidgetActionType.OPEN_INSTALLED_APP -> action.packageName?.let {
                context.packageManager.getLaunchIntentForPackage(it)
            }
        } ?: return null
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
