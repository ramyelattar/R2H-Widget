package com.r2h_widget.widget.battery

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryWidgetReceiver : AppWidgetProvider() {


    override fun onDisabled(context: Context) {
        BatteryMonitorService.stop(context)
        super.onDisabled(context)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in BATTERY_ACTIONS || intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            updateWidgets(context, manager, manager.getAppWidgetIds(componentName(context)))
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
        newOptions: android.os.Bundle,
    ) {
        updateWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).typedWidgetInstanceRepository
                ids.forEach { appWidgetId ->
                    val instance = repository.get(appWidgetId)
                        ?.takeIf { it.configuration is TypedWidgetConfiguration.Battery }
                        ?: TypedWidgetInstance(
                            appWidgetId,
                            DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Battery(BatteryWidgetConfiguration()),
                        ).also { repository.save(it) }
                    val configuration = (instance.configuration as? TypedWidgetConfiguration.Battery)?.value
                        ?: BatteryWidgetConfiguration()
                    manager.updateAppWidget(
                        appWidgetId,
                        BatteryWidgetRenderer.render(
                            context,
                            configuration,
                            options = manager.getAppWidgetOptions(appWidgetId),
                            appWidgetId = appWidgetId,
                            productId = instance.productId,
                        ),
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val DEFAULT_PRODUCT_ID = "battery"
        const val ACTION_REFRESH = "com.r2h_widget.widget.battery.action.REFRESH"

        fun requestRefresh(context: Context) {
            context.sendBroadcast(
                Intent(context, BatteryWidgetReceiver::class.java)
                    .setAction(ACTION_REFRESH),
            )
        }
        private val BATTERY_ACTIONS = setOf(
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
        )

        private fun componentName(context: Context) =
            android.content.ComponentName(context, BatteryWidgetReceiver::class.java)

        suspend fun update(
            context: Context,
            appWidgetId: Int,
            configuration: BatteryWidgetConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                BatteryWidgetRenderer.render(
                    context,
                    configuration,
                    options = appWidgetManager.getAppWidgetOptions(appWidgetId),
                    appWidgetId = appWidgetId,
                    productId = productId,
                ),
            )
        }
    }
}



