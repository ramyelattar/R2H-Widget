package com.r2h_widget.widget.analog

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Launcher provider for the Analog Clock product.
 *
 * Unlike the digital clock, RemoteViews cannot host a self-updating analog dial, so
 * this provider re-draws the dial bitmap on a minute cadence: after every system
 * update event and through a lightweight inexact alarm scheduled at the next minute
 * boundary. While the device dozes, the system may defer those alarms; the dial
 * always catches up on the next tick.
 */
class AnalogClockWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
        scheduleNextMinuteTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_MINUTE_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, AnalogClockWidgetReceiver::class.java))
                updateWidgets(context, manager, ids)
                scheduleNextMinuteTick(context)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).typedWidgetInstanceRepository
                appWidgetIds.forEach { repository.delete(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onDisabled(context: Context) {
        cancelMinuteTick(context)
    }

    private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).typedWidgetInstanceRepository
                ids.forEach { appWidgetId ->
                    val instance = repository.get(appWidgetId)
                        ?.takeIf { it.configuration is TypedWidgetConfiguration.Analog }
                        ?: TypedWidgetInstance(
                            appWidgetId,
                            DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Analog(AnalogClockConfiguration()),
                        ).also { repository.save(it) }
                    val configuration = (instance.configuration as? TypedWidgetConfiguration.Analog)?.value
                        ?: AnalogClockConfiguration()
                    manager.updateAppWidget(
                        appWidgetId,
                        AnalogClockRemoteViewsRenderer.render(
                            context = context,
                            configuration = configuration,
                            appWidgetId = appWidgetId,
                            productId = instance.productId,
                            options = manager.getAppWidgetOptions(appWidgetId),
                        ),
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNextMinuteTick(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextMinute = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val pendingIntent = minuteTickPendingIntent(context)
        // Inexact and allowed while idle: Doze may delay a tick, never wake the device for it.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pendingIntent)
    }

    private fun cancelMinuteTick(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(minuteTickPendingIntent(context))
    }

    private fun minuteTickPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        MINUTE_TICK_REQUEST_CODE,
        Intent(context, AnalogClockWidgetReceiver::class.java).setAction(ACTION_MINUTE_TICK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val DEFAULT_PRODUCT_ID = "analog-clock"
        const val ACTION_MINUTE_TICK = "com.r2h_widget.action.ANALOG_MINUTE_TICK"
        private const val MINUTE_TICK_REQUEST_CODE = 4021

        suspend fun update(
            context: Context,
            appWidgetId: Int,
            configuration: AnalogClockConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                AnalogClockRemoteViewsRenderer.render(
                    context = context,
                    configuration = configuration,
                    appWidgetId = appWidgetId,
                    productId = productId,
                    options = appWidgetManager.getAppWidgetOptions(appWidgetId),
                ),
            )
        }
    }
}
