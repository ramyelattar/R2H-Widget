package com.r2h_widget.widget.search

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

class SearchWidgetReceiver : AppWidgetProvider() {
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
                        ?.takeIf { it.configuration is TypedWidgetConfiguration.Search }
                        ?: TypedWidgetInstance(
                            appWidgetId,
                            DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Search(SearchWidgetConfiguration()),
                        ).also { repository.save(it) }
                    val configuration = (instance.configuration as? TypedWidgetConfiguration.Search)?.value
                        ?: SearchWidgetConfiguration()
                    manager.updateAppWidget(
                        appWidgetId,
                        SearchWidgetRenderer.render(
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
        const val DEFAULT_PRODUCT_ID = "search"

        suspend fun update(
            context: Context,
            appWidgetId: Int,
            configuration: SearchWidgetConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context),
        ) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                SearchWidgetRenderer.render(
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
