package com.r2h_widget.widget.pinning

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.r2h_widget.catalog.HomeWidgetAvailability
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import com.r2h_widget.data.TypedWidgetInstanceCodec
import com.r2h_widget.data.WidgetInstance
import com.r2h_widget.data.WidgetInstanceCodec
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.analog.AnalogClockRemoteViewsRenderer
import com.r2h_widget.widget.analog.AnalogClockWidgetReceiver
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryMonitorService
import com.r2h_widget.widget.battery.BatteryWidgetReceiver
import com.r2h_widget.widget.battery.BatteryWidgetRenderer
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.DigitalClockRemoteViewsRenderer
import com.r2h_widget.widget.clock.DigitalClockWidgetReceiver
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.search.SearchWidgetReceiver
import com.r2h_widget.widget.search.SearchWidgetRenderer
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetReceiver
import com.r2h_widget.widget.music.MusicWidgetRenderer
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherLocationService
import com.r2h_widget.widget.weather.WeatherWidgetReceiver
import com.r2h_widget.widget.weather.WeatherWidgetRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

sealed interface WidgetPinningResult {
    data object Requested : WidgetPinningResult
    data object Unsupported : WidgetPinningResult
    data class Failed(val message: String) : WidgetPinningResult
}

interface WidgetPinningController {
    fun requestDigitalClockPin(
        productId: String,
        configuration: com.r2h_widget.widget.clock.DigitalClockConfiguration,
    ): WidgetPinningResult

    fun updateDigitalClock(
        appWidgetId: Int,
        productId: String,
        configuration: DigitalClockConfiguration,
    ): WidgetPinningResult = WidgetPinningResult.Unsupported

    fun requestAnalogClockPin(productId: String, configuration: AnalogClockConfiguration): WidgetPinningResult =
        WidgetPinningResult.Unsupported

    fun updateAnalogClock(
        appWidgetId: Int,
        productId: String,
        configuration: AnalogClockConfiguration,
    ): WidgetPinningResult = WidgetPinningResult.Unsupported

    fun requestBatteryPin(productId: String, configuration: BatteryWidgetConfiguration): WidgetPinningResult =
        WidgetPinningResult.Unsupported

    fun updateBattery(
        appWidgetId: Int,
        productId: String,
        configuration: BatteryWidgetConfiguration,
    ): WidgetPinningResult = WidgetPinningResult.Unsupported

    fun requestWeatherPin(productId: String, configuration: WeatherWidgetConfiguration): WidgetPinningResult =
        WidgetPinningResult.Unsupported

    fun updateWeather(
        appWidgetId: Int,
        productId: String,
        configuration: WeatherWidgetConfiguration,
    ): WidgetPinningResult = WidgetPinningResult.Unsupported

    fun requestSearchPin(productId: String, configuration: SearchWidgetConfiguration): WidgetPinningResult =
        WidgetPinningResult.Unsupported

    fun updateSearch(
        appWidgetId: Int,
        productId: String,
        configuration: SearchWidgetConfiguration,
    ): WidgetPinningResult = WidgetPinningResult.Unsupported

    fun requestMusicPin(
        productId: String,
        configuration: MusicWidgetConfiguration,
    ): WidgetPinningResult =
        WidgetPinningResult.Unsupported

    fun updateMusic(
        appWidgetId: Int,
        productId: String,
        configuration: MusicWidgetConfiguration,
    ): WidgetPinningResult =
        WidgetPinningResult.Unsupported
}

class AndroidWidgetPinningController(
    private val context: Context,
) : WidgetPinningController {
    override fun requestDigitalClockPin(
        productId: String,
        configuration: DigitalClockConfiguration,
    ): WidgetPinningResult {
        return requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.DIGITAL_CLOCK,
            provider = DigitalClockWidgetReceiver::class.java,
            configurationPayload = WidgetInstanceCodec.encode(WidgetInstance(0, productId, configuration)),
            preview = DigitalClockRemoteViewsRenderer.render(context, configuration),
        )
    }

    override fun updateDigitalClock(
        appWidgetId: Int,
        productId: String,
        configuration: DigitalClockConfiguration,
    ): WidgetPinningResult {
        val definition = WidgetCatalog.findById(productId)
        if (definition?.homeWidgetAvailability != HomeWidgetAvailability.DIGITAL_CLOCK) {
            return WidgetPinningResult.Failed("This product is not a digital clock widget.")
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppDependencies(context).widgetInstanceRepository.save(
                WidgetInstance(appWidgetId, productId, configuration),
            )
            DigitalClockWidgetReceiver.update(
                context = context,
                appWidgetId = appWidgetId,
                productId = productId,
                configuration = configuration,
            )
        }
        return WidgetPinningResult.Requested
    }

    override fun requestAnalogClockPin(productId: String, configuration: AnalogClockConfiguration): WidgetPinningResult =
        requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.ANALOG_CLOCK,
            provider = AnalogClockWidgetReceiver::class.java,
            configurationPayload = TypedWidgetInstanceCodec.encode(
                TypedWidgetInstance(0, productId, TypedWidgetConfiguration.Analog(configuration)),
            ),
            preview = AnalogClockRemoteViewsRenderer.render(context, configuration),
        )

    override fun updateAnalogClock(
        appWidgetId: Int,
        productId: String,
        configuration: AnalogClockConfiguration,
    ): WidgetPinningResult {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppDependencies(context).typedWidgetInstanceRepository.save(
                TypedWidgetInstance(appWidgetId, productId, TypedWidgetConfiguration.Analog(configuration)),
            )
            AnalogClockWidgetReceiver.update(context, appWidgetId, configuration, productId)
        }
        return WidgetPinningResult.Requested
    }

    override fun requestBatteryPin(productId: String, configuration: BatteryWidgetConfiguration): WidgetPinningResult =
        requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.BATTERY,
            provider = BatteryWidgetReceiver::class.java,
            configurationPayload = TypedWidgetInstanceCodec.encode(
                TypedWidgetInstance(0, productId, TypedWidgetConfiguration.Battery(configuration)),
            ),
            preview = BatteryWidgetRenderer.render(context, configuration),
        )

    override fun updateBattery(
        appWidgetId: Int,
        productId: String,
        configuration: BatteryWidgetConfiguration,
    ): WidgetPinningResult {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppDependencies(context).typedWidgetInstanceRepository.save(
                TypedWidgetInstance(appWidgetId, productId, TypedWidgetConfiguration.Battery(configuration)),
            )
            BatteryWidgetReceiver.update(context, appWidgetId, configuration, productId)
        }
        return WidgetPinningResult.Requested
    }

    override fun requestWeatherPin(productId: String, configuration: WeatherWidgetConfiguration): WidgetPinningResult =
        requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.WEATHER,
            provider = WeatherWidgetReceiver::class.java,
            configurationPayload = TypedWidgetInstanceCodec.encode(
                TypedWidgetInstance(0, productId, TypedWidgetConfiguration.Weather(configuration)),
            ),
            preview = WeatherWidgetRenderer.unavailablePreview(context, configuration),
        )

    override fun updateWeather(
        appWidgetId: Int,
        productId: String,
        configuration: WeatherWidgetConfiguration,
    ): WidgetPinningResult {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppDependencies(context).typedWidgetInstanceRepository.save(
                TypedWidgetInstance(appWidgetId, productId, TypedWidgetConfiguration.Weather(configuration)),
            )
            WeatherWidgetReceiver.update(context, appWidgetId, configuration, productId)
        }
        return WidgetPinningResult.Requested
    }

    override fun requestSearchPin(productId: String, configuration: SearchWidgetConfiguration): WidgetPinningResult =
        requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.SEARCH,
            provider = SearchWidgetReceiver::class.java,
            configurationPayload = TypedWidgetInstanceCodec.encode(
                TypedWidgetInstance(0, productId, TypedWidgetConfiguration.Search(configuration)),
            ),
            preview = SearchWidgetRenderer.render(context, configuration),
        )

    override fun updateSearch(
        appWidgetId: Int,
        productId: String,
        configuration: SearchWidgetConfiguration,
    ): WidgetPinningResult {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            AppDependencies(context).typedWidgetInstanceRepository.save(
                TypedWidgetInstance(appWidgetId, productId, TypedWidgetConfiguration.Search(configuration)),
            )
            SearchWidgetReceiver.update(context, appWidgetId, configuration, productId)
        }
        return WidgetPinningResult.Requested
    }

    override fun requestMusicPin(
        productId: String,
        configuration: MusicWidgetConfiguration,
    ): WidgetPinningResult =
        requestPin(
            productId = productId,
            availability = HomeWidgetAvailability.MUSIC,
            provider = MusicWidgetReceiver::class.java,
            configurationPayload =
                TypedWidgetInstanceCodec.encode(
                    TypedWidgetInstance(
                        0,
                        productId,
                        TypedWidgetConfiguration.Music(
                            configuration,
                        ),
                    ),
                ),
            preview = MusicWidgetRenderer.render(
                context = context,
                appWidgetId = 0,
                configuration = configuration,
                snapshot = null,
                interactive = false,
            ),
        )

    override fun updateMusic(
        appWidgetId: Int,
        productId: String,
        configuration: MusicWidgetConfiguration,
    ): WidgetPinningResult {
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO,
        ).launch {
            AppDependencies(context)
                .typedWidgetInstanceRepository
                .save(
                    TypedWidgetInstance(
                        appWidgetId,
                        productId,
                        TypedWidgetConfiguration.Music(
                            configuration,
                        ),
                    ),
                )

            MusicWidgetReceiver.update(
                context = context,
                appWidgetId = appWidgetId,
                configuration = configuration,
                productId = productId,
            )
        }

        return WidgetPinningResult.Requested
    }
    private fun requestPin(
        productId: String,
        availability: HomeWidgetAvailability,
        provider: Class<out android.appwidget.AppWidgetProvider>,
        configurationPayload: String,
        preview: android.widget.RemoteViews,
    ): WidgetPinningResult {
        val definition = WidgetCatalog.findById(productId)
        if (definition?.homeWidgetAvailability != availability) {
            return WidgetPinningResult.Failed("This product is not available as a home widget.")
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (!appWidgetManager.isRequestPinAppWidgetSupported) return WidgetPinningResult.Unsupported

        val callbackIntent = Intent(context, WidgetPinSuccessReceiver::class.java)
            .putExtra(EXTRA_PRODUCT_ID, productId)
            .putExtra(
                if (availability == HomeWidgetAvailability.DIGITAL_CLOCK) EXTRA_CONFIGURATION else EXTRA_TYPED_CONFIGURATION,
                configurationPayload,
            )
        val callback = PendingIntent.getBroadcast(
            context,
            configurationPayload.hashCode(),
            callbackIntent,
            // requestPinAppWidget fills EXTRA_APPWIDGET_ID into this callback.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val extras = Bundle().apply {
            putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, preview)
        }
        return try {
            if (appWidgetManager.requestPinAppWidget(ComponentName(context, provider), extras, callback)) {
                WidgetPinningResult.Requested
            } else {
                WidgetPinningResult.Unsupported
            }
        } catch (exception: IllegalStateException) {
            WidgetPinningResult.Failed(exception.message ?: "The launcher rejected the pin request.")
        } catch (exception: SecurityException) {
            WidgetPinningResult.Failed(exception.message ?: "The launcher rejected the pin request.")
        }
    }
}

class WidgetPinSuccessReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
            ?: WidgetProductIds.DIGITAL_CLOCK
        val definition = WidgetCatalog.findById(productId)

        if (definition?.homeWidgetAvailability == HomeWidgetAvailability.BATTERY) {
            BatteryMonitorService.start(context)
        }

        if (definition?.homeWidgetAvailability == HomeWidgetAvailability.WEATHER) {
            try {
                WeatherLocationService.start(context)
            } catch (exception: SecurityException) {
                android.util.Log.w(
                    "WidgetPinSuccess",
                    "Weather location service could not start because location permission is unavailable.",
                    exception,
                )
            } catch (exception: android.app.ForegroundServiceStartNotAllowedException) {
                android.util.Log.w(
                    "WidgetPinSuccess",
                    "Weather location service foreground start was rejected.",
                    exception,
                )
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (definition?.homeWidgetAvailability) {
                    HomeWidgetAvailability.DIGITAL_CLOCK,
                    null,
                    -> {
                        val instance = intent.getStringExtra(EXTRA_CONFIGURATION)
                            ?.let { WidgetInstanceCodec.decode(appWidgetId, it) }
                            ?: WidgetInstance(appWidgetId, productId, DigitalClockConfiguration())
                        AppDependencies(context).widgetInstanceRepository.save(instance)
                        DigitalClockWidgetReceiver.update(
                            context = context,
                            appWidgetId = appWidgetId,
                            productId = instance.productId,
                            configuration = instance.configuration,
                        )
                    }
                    HomeWidgetAvailability.ANALOG_CLOCK -> {
                        val instance = typedInstanceFrom(
                            intent,
                            appWidgetId,
                            productId,
                        ) { TypedWidgetConfiguration.Analog(AnalogClockConfiguration()) }
                        val configuration = (instance.configuration as? TypedWidgetConfiguration.Analog)?.value
                            ?: AnalogClockConfiguration()
                        AppDependencies(context).typedWidgetInstanceRepository.save(instance)
                        AnalogClockWidgetReceiver.update(context, appWidgetId, configuration, instance.productId)
                    }
                    HomeWidgetAvailability.BATTERY -> {
                        val instance = typedInstanceFrom(
                            intent,
                            appWidgetId,
                            productId,
                        ) { TypedWidgetConfiguration.Battery(BatteryWidgetConfiguration()) }
                        val configuration = (instance.configuration as? TypedWidgetConfiguration.Battery)?.value
                            ?: BatteryWidgetConfiguration()
                        AppDependencies(context).typedWidgetInstanceRepository.save(instance)
                        BatteryWidgetReceiver.update(context, appWidgetId, configuration, instance.productId)
                    }
                    HomeWidgetAvailability.WEATHER -> {
                        val instance = typedInstanceFrom(
                            intent,
                            appWidgetId,
                            productId,
                        ) { TypedWidgetConfiguration.Weather(WeatherWidgetConfiguration()) }
                        val configuration = (instance.configuration as? TypedWidgetConfiguration.Weather)?.value
                            ?: WeatherWidgetConfiguration()
                        AppDependencies(context).typedWidgetInstanceRepository.save(instance)
                        WeatherWidgetReceiver.update(context, appWidgetId, configuration, instance.productId)
                    }
                    HomeWidgetAvailability.SEARCH -> {
                        val instance = typedInstanceFrom(
                            intent,
                            appWidgetId,
                            productId,
                        ) { TypedWidgetConfiguration.Search(SearchWidgetConfiguration()) }
                        val configuration = (instance.configuration as? TypedWidgetConfiguration.Search)?.value
                            ?: SearchWidgetConfiguration()
                        AppDependencies(context).typedWidgetInstanceRepository.save(instance)
                        SearchWidgetReceiver.update(context, appWidgetId, configuration, instance.productId)
                    }
                    HomeWidgetAvailability.MUSIC -> {
                        val instance = typedInstanceFrom(
                            intent,
                            appWidgetId,
                            productId,
                        ) {
                            TypedWidgetConfiguration.Music(
                                MusicWidgetConfiguration(),
                            )
                        }

                        val configuration =
                            (
                                instance.configuration as?
                                    TypedWidgetConfiguration.Music
                            )?.value
                                ?: MusicWidgetConfiguration()

                        AppDependencies(context)
                            .typedWidgetInstanceRepository
                            .save(instance)

                        MusicWidgetReceiver.update(
                            context = context,
                            appWidgetId = appWidgetId,
                            configuration = configuration,
                            productId = instance.productId,
                        )
                    }
                    HomeWidgetAvailability.NONE -> Unit
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private inline fun typedInstanceFrom(
        intent: Intent,
        appWidgetId: Int,
        productId: String,
        fallback: () -> TypedWidgetConfiguration,
    ): TypedWidgetInstance = intent.getStringExtra(EXTRA_TYPED_CONFIGURATION)
        ?.let { TypedWidgetInstanceCodec.decode(appWidgetId, it) }
        ?: TypedWidgetInstance(appWidgetId, productId, fallback())
}

const val EXTRA_PRODUCT_ID = "com.r2h_widget.extra.PRODUCT_ID"
const val EXTRA_CONFIGURATION = "com.r2h_widget.extra.CONFIGURATION"
const val EXTRA_TYPED_CONFIGURATION = "com.r2h_widget.extra.TYPED_CONFIGURATION"







