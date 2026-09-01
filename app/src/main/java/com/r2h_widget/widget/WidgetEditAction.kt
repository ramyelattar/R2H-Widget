package com.r2h_widget.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.r2h_widget.MainActivity
import com.r2h_widget.widget.clock.EXTRA_EDIT_APP_WIDGET_ID
import com.r2h_widget.widget.clock.EXTRA_EDIT_PRODUCT_ID

object WidgetEditAction {
    fun attach(
        context: Context,
        remoteViews: RemoteViews,
        rootId: Int,
        appWidgetId: Int,
        productId: String,
    ) {
        val pendingIntent = pendingEditIntent(context, appWidgetId, productId) ?: return
        remoteViews.setOnClickPendingIntent(rootId, pendingIntent)
    }

    /** The PendingIntent that opens this widget instance's studio screen, or null for pin previews. */
    fun pendingEditIntent(
        context: Context,
        appWidgetId: Int,
        productId: String,
    ): PendingIntent? {
        if (appWidgetId < 0) return null
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_EDIT_APP_WIDGET_ID, appWidgetId)
            .putExtra(EXTRA_EDIT_PRODUCT_ID, productId)
        return PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
