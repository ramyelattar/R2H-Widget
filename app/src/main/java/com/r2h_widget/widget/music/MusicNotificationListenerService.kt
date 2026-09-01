package com.r2h_widget.widget.music

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class MusicNotificationListenerService :
    NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        MusicWidgetReceiver.refreshAll(
            applicationContext,
        )
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification?,
    ) {
        val notification = sbn?.notification ?: return

        val isMediaNotification =
            notification.category ==
                Notification.CATEGORY_TRANSPORT ||
                notification.extras.containsKey(
                    Notification.EXTRA_MEDIA_SESSION,
                )

        if (isMediaNotification) {
            MusicWidgetReceiver.refreshAll(
                applicationContext,
            )
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
    ) {
        val notification = sbn?.notification ?: return

        val isMediaNotification =
            notification.category ==
                Notification.CATEGORY_TRANSPORT ||
                notification.extras.containsKey(
                    Notification.EXTRA_MEDIA_SESSION,
                )

        if (isMediaNotification) {
            MusicWidgetReceiver.refreshAll(
                applicationContext,
            )
        }
    }
}
