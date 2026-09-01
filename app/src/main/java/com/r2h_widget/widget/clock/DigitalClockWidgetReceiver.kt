package com.r2h_widget.widget.clock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.WidgetInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DigitalClockWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).widgetInstanceRepository
                appWidgetIds.forEach { appWidgetId ->
                    val instance = repository.get(appWidgetId) ?: WidgetInstance(
                        appWidgetId = appWidgetId,
                        productId = DEFAULT_PRODUCT_ID,
                        configuration = DigitalClockConfiguration(),
                    ).also { defaultInstance ->
                        repository.save(defaultInstance)
                    }
                    appWidgetManager.updateAppWidget(
                        appWidgetId,
                        DigitalClockRemoteViewsRenderer.render(
                            context = context,
                            configuration = instance.configuration,
                            appWidgetId = appWidgetId,
                            productId = instance.productId,
                            options = appWidgetManager.getAppWidgetOptions(appWidgetId),
                        ),
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val repository = AppDependencies(context).widgetInstanceRepository
                appWidgetIds.forEach { appWidgetId -> repository.delete(appWidgetId) }
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
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    companion object {
        const val DEFAULT_PRODUCT_ID = "digital-clock"

        suspend fun update(
            context: Context,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
            appWidgetId: Int,
            productId: String = DEFAULT_PRODUCT_ID,
            configuration: DigitalClockConfiguration = DigitalClockConfiguration(),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                DigitalClockRemoteViewsRenderer.render(
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
