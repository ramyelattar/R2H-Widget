package com.r2h_widget.widget.weather

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
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

    private fun updateWidgets(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).typedWidgetInstanceRepository
                ids.forEach { appWidgetId ->
                    val instance = repository.get(appWidgetId)
                        ?.takeIf { it.configuration is TypedWidgetConfiguration.Weather }
                        ?: TypedWidgetInstance(
                            appWidgetId,
                            DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Weather(WeatherWidgetConfiguration()),
                        ).also { repository.save(it) }
                    val configuration = (instance.configuration as? TypedWidgetConfiguration.Weather)?.value
                        ?: WeatherWidgetConfiguration()
                    manager.updateAppWidget(
                        appWidgetId,
                        WeatherWidgetRenderer.render(
                            context,
                            configuration,
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

    companion object {
        const val DEFAULT_PRODUCT_ID = "weather"

        suspend fun update(
            context: Context,
            appWidgetId: Int,
            configuration: WeatherWidgetConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                WeatherWidgetRenderer.render(
                    context,
                    configuration,
                    appWidgetId = appWidgetId,
                    productId = productId,
                    options = appWidgetManager.getAppWidgetOptions(appWidgetId),
                ),
            )
        }
    }
}
