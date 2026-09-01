package com.r2h_widget.widget.battery

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder

class BatteryMonitorService : Service() {

    private var receiverRegistered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                BatteryWidgetReceiver.requestRefresh(this@BatteryMonitorService)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            Context.RECEIVER_EXPORTED,
        )
        receiverRegistered = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val widgetIds = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(
                ComponentName(this, BatteryWidgetReceiver::class.java),
            )

        if (widgetIds.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        BatteryWidgetReceiver.requestRefresh(this)

        return START_STICKY
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(batteryReceiver)
            receiverRegistered = false
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Battery widget",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Keeps the home-screen battery widget up to date"
            setShowBadge(false)
        }

        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Battery widget active")
            .setContentText("Keeping the battery level up to date")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "battery_widget_monitor"
        private const val NOTIFICATION_ID = 2401

        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, BatteryMonitorService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, BatteryMonitorService::class.java),
            )
        }
    }
}
