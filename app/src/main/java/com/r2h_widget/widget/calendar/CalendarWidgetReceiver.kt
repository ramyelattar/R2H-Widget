package com.r2h_widget.widget.calendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarWidgetReceiver : AppWidgetProvider() {
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
                    val instance = repository.get(appWidgetId)?.takeIf { it.productId.startsWith("calendar-") }
                        ?: TypedWidgetInstance(
                            appWidgetId,
                            DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Calendar(CalendarWidgetConfiguration()),
                        ).also { repository.save(it) }
                    val configuration = (instance.configuration as? TypedWidgetConfiguration.Calendar)?.value
                        ?: CalendarWidgetConfiguration()
                    manager.updateAppWidget(
                        appWidgetId,
                        CalendarWidgetRenderer.render(
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
        const val DEFAULT_PRODUCT_ID = "calendar-foundation"

        suspend fun update(
            context: Context,
            appWidgetId: Int,
            configuration: CalendarWidgetConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                CalendarWidgetRenderer.render(
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
