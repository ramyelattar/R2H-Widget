package com.r2h_widget.widget.music

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicWidgetReceiver : AppWidgetProvider() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            ACTION_PREVIOUS,
            ACTION_PLAY_PAUSE,
            ACTION_NEXT,
            ACTION_REFRESH,
            -> {
                val pending = goAsync()

                CoroutineScope(
                    SupervisorJob() + Dispatchers.IO,
                ).launch {
                    try {
                        handleAction(
                            context.applicationContext,
                            intent,
                        )
                    } finally {
                        pending.finish()
                    }
                }

                return
            }
        }

        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pending = goAsync()

        CoroutineScope(
            SupervisorJob() + Dispatchers.IO,
        ).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    updateFromRepository(
                        context.applicationContext,
                        appWidgetId,
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        val pending = goAsync()
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO,
        ).launch {
            try {
                updateFromRepository(context.applicationContext, appWidgetId)
            } finally {
                pending.finish()
            }
        }
    }

    override fun onDeleted(
        context: Context,
        appWidgetIds: IntArray,
    ) {
        val pending = goAsync()

        CoroutineScope(
            SupervisorJob() + Dispatchers.IO,
        ).launch {
            try {
                val repository =
                    AppDependencies(context)
                        .typedWidgetInstanceRepository

                appWidgetIds.forEach { appWidgetId ->
                    repository.delete(appWidgetId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handleAction(
        context: Context,
        intent: Intent,
    ) {
        val appWidgetId =
            intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )

        if (
            appWidgetId ==
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {
            return
        }

        val instance =
            AppDependencies(context)
                .typedWidgetInstanceRepository
                .get(appWidgetId)
                ?: return

        val configuration =
            (
                instance.configuration as?
                    TypedWidgetConfiguration.Music
            )?.value
                ?: return

        when (intent.action) {
            ACTION_PREVIOUS ->
                MusicSessionAccess.dispatch(
                    context,
                    configuration.selectedPlayerPackage,
                    MusicTransportCommand.PREVIOUS,
                )

            ACTION_PLAY_PAUSE ->
                MusicSessionAccess.dispatch(
                    context,
                    configuration.selectedPlayerPackage,
                    MusicTransportCommand.PLAY_PAUSE,
                )

            ACTION_NEXT ->
                MusicSessionAccess.dispatch(
                    context,
                    configuration.selectedPlayerPackage,
                    MusicTransportCommand.NEXT,
                )

            ACTION_REFRESH -> Unit
        }

        if (intent.action != ACTION_REFRESH) {
            delay(180L)
        }

        update(
            context,
            appWidgetId,
            configuration,
            instance.productId,
        )
    }

    companion object {
        const val ACTION_PREVIOUS =
            "com.r2h_widget.widget.music.PREVIOUS"

        const val ACTION_PLAY_PAUSE =
            "com.r2h_widget.widget.music.PLAY_PAUSE"

        const val ACTION_NEXT =
            "com.r2h_widget.widget.music.NEXT"

        const val ACTION_REFRESH =
            "com.r2h_widget.widget.music.REFRESH"

        const val DEFAULT_PRODUCT_ID =
            WidgetProductIds.MUSIC

        fun update(
            context: Context,
            appWidgetId: Int,
            configuration: MusicWidgetConfiguration,
            productId: String = DEFAULT_PRODUCT_ID,
        ) {
            val snapshot =
                MusicSessionAccess.snapshot(
                    context,
                    configuration.selectedPlayerPackage,
                )

            val views =
                MusicWidgetRenderer.render(
                    context = context,
                    appWidgetId = appWidgetId,
                    configuration = configuration,
                    snapshot = snapshot,
                )

            AppWidgetManager
                .getInstance(context)
                .updateAppWidget(
                    appWidgetId,
                    views,
                )
        }

        fun refreshAll(context: Context) {
            CoroutineScope(
                SupervisorJob() + Dispatchers.IO,
            ).launch {
                val manager =
                    AppWidgetManager.getInstance(context)

                val ids =
                    manager.getAppWidgetIds(
                        ComponentName(
                            context,
                            MusicWidgetReceiver::class.java,
                        ),
                    )

                ids.forEach { appWidgetId ->
                    updateFromRepository(
                        context.applicationContext,
                        appWidgetId,
                    )
                }
            }
        }

        private suspend fun updateFromRepository(
            context: Context,
            appWidgetId: Int,
        ) {
            val instance =
                AppDependencies(context)
                    .typedWidgetInstanceRepository
                    .get(appWidgetId)
                ?: return

            val configuration =
                (
                    instance.configuration as?
                        TypedWidgetConfiguration.Music
                )?.value
                ?: return

            update(
                context = context,
                appWidgetId = appWidgetId,
                configuration = configuration,
                productId = instance.productId,
            )
        }
    }
}
