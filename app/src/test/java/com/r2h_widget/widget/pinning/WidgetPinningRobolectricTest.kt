package com.r2h_widget.widget.pinning

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Looper
import com.r2h_widget.data.AppDependencies
import com.r2h_widget.data.TypedWidgetConfiguration
import com.r2h_widget.data.TypedWidgetInstance
import com.r2h_widget.data.TypedWidgetInstanceCodec
import com.r2h_widget.data.WidgetInstance
import com.r2h_widget.data.WidgetInstanceCodec
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.widget.clock.ClockContentConfiguration
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.DigitalClockWidgetReceiver
import com.r2h_widget.widget.clock.WidgetTypographyConfiguration
import com.r2h_widget.widget.clock.defaultClockConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetReceiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetPinningRobolectricTest {
    private lateinit var context: Context
    private lateinit var repository: com.r2h_widget.data.WidgetInstanceRepository

    @Before
    fun setUp() {
        context = org.robolectric.RuntimeEnvironment.getApplication()
        repository = AppDependencies(context).widgetInstanceRepository
    }

    @Test
    fun requestPinCallback_persistsLauncherWidgetIdAndExactConfiguration() = runBlocking {
        val configuration = obviousConfiguration()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        shadowOf(appWidgetManager).setRequestPinAppWidgetSupported(true)

        val result = AndroidWidgetPinningController(context).requestDigitalClockPin(
            productId = DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID,
            configuration = configuration,
        )

        assertEquals(WidgetPinningResult.Requested, result)
        shadowOf(Looper.getMainLooper()).idle()
        val appWidgetId = appWidgetManager
            .getAppWidgetIds(ComponentName(context, DigitalClockWidgetReceiver::class.java))
            .maxOrNull()
        assertNotNull(appWidgetId)

        val saved = awaitInstanceOrNull(requireNotNull(appWidgetId), configuration)
        assertNotNull(
            "Pin success callback did not persist the launcher-supplied appWidgetId and configuration",
            saved,
        )
        val persisted = requireNotNull(saved)
        assertEquals(DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID, persisted.productId)
        assertEquals(configuration, persisted.configuration)
    }

    @Test
    fun successCallback_overwritesDefaultCreatedByProvider() = runBlocking {
        val appWidgetId = 7401
        repository.delete(appWidgetId)
        val defaultInstance = WidgetInstance(
            appWidgetId = appWidgetId,
            productId = DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID,
            configuration = DigitalClockConfiguration(),
        )
        // Model the default instance written by DigitalClockWidgetReceiver.onUpdate()
        // before the pin-success callback arrives.
        repository.save(defaultInstance)
        assertEquals(defaultInstance, repository.get(appWidgetId))
        assertEquals(ClockFontFamily.SYSTEM, defaultInstance.configuration.typography.timeFontFamily)
        assertEquals(ClockFontFamily.SYSTEM, defaultInstance.configuration.typography.dateFontFamily)

        val configuration = obviousConfiguration()
        WidgetPinSuccessReceiver().onReceive(
            context,
            Intent(context, WidgetPinSuccessReceiver::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_PRODUCT_ID, DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID)
                putExtra(
                    EXTRA_CONFIGURATION,
                    WidgetInstanceCodec.encode(
                        WidgetInstance(
                            appWidgetId = 0,
                            productId = DigitalClockWidgetReceiver.DEFAULT_PRODUCT_ID,
                            configuration = configuration,
                        ),
                    ),
                )
            },
        )

        val saved = awaitInstance(appWidgetId, configuration)
        assertEquals(configuration, saved.configuration)
    }

    @Test
    fun everyCatalogProduct_pinsThroughItsOwnRealProvider() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        shadowOf(appWidgetManager).setRequestPinAppWidgetSupported(true)
        val controller = AndroidWidgetPinningController(context)

        assertEquals(
            WidgetPinningResult.Requested,
            controller.requestDigitalClockPin(WidgetCatalog.digitalClock.id, WidgetCatalog.digitalClock.defaultClockConfiguration()),
        )
        assertEquals(
            WidgetPinningResult.Requested,
            controller.requestAnalogClockPin(WidgetCatalog.analogClock.id, com.r2h_widget.widget.analog.AnalogClockConfiguration()),
        )
        assertEquals(
            WidgetPinningResult.Requested,
            controller.requestBatteryPin(WidgetCatalog.battery.id, BatteryWidgetConfiguration()),
        )
        assertEquals(
            WidgetPinningResult.Requested,
            controller.requestWeatherPin(WidgetCatalog.weather.id, com.r2h_widget.widget.weather.WeatherWidgetConfiguration()),
        )
        assertEquals(
            WidgetPinningResult.Requested,
            controller.requestSearchPin(WidgetCatalog.search.id, com.r2h_widget.widget.search.SearchWidgetConfiguration()),
        )
    }

    @Test
    fun batteryPinCallback_persistsConfigurationAgainstLauncherId() = runBlocking {
        val configuration = BatteryWidgetConfiguration(
            showPercentage = false,
            showChargingState = true,
            showPlugSource = true,
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        shadowOf(appWidgetManager).setRequestPinAppWidgetSupported(true)

        val result = AndroidWidgetPinningController(context).requestBatteryPin(
            productId = BatteryWidgetReceiver.DEFAULT_PRODUCT_ID,
            configuration = configuration,
        )

        assertEquals(WidgetPinningResult.Requested, result)
        shadowOf(Looper.getMainLooper()).idle()
        val appWidgetId = appWidgetManager
            .getAppWidgetIds(ComponentName(context, BatteryWidgetReceiver::class.java))
            .maxOrNull()
        assertNotNull(appWidgetId)
        val repository = AppDependencies(context).typedWidgetInstanceRepository
        val saved = withTimeoutOrNull(5_000) {
            while (true) {
                val instance = repository.get(requireNotNull(appWidgetId))
                if (instance?.configuration == TypedWidgetConfiguration.Battery(configuration)) return@withTimeoutOrNull instance
                delay(25)
            }
            null
        }
        assertNotNull(saved)
        assertEquals(requireNotNull(appWidgetId), requireNotNull(saved).appWidgetId)
    }

    @Test
    fun typedSuccessCallback_overwritesDefaultCreatedByProvider() = runBlocking {
        val appWidgetId = 7402
        val repository = AppDependencies(context).typedWidgetInstanceRepository
        repository.save(
            TypedWidgetInstance(
                appWidgetId,
                BatteryWidgetReceiver.DEFAULT_PRODUCT_ID,
                TypedWidgetConfiguration.Battery(BatteryWidgetConfiguration()),
            ),
        )
        val requested = BatteryWidgetConfiguration(showPercentage = false, showPlugSource = true)

        WidgetPinSuccessReceiver().onReceive(
            context,
            Intent(context, WidgetPinSuccessReceiver::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                putExtra(EXTRA_PRODUCT_ID, BatteryWidgetReceiver.DEFAULT_PRODUCT_ID)
                putExtra(
                    EXTRA_TYPED_CONFIGURATION,
                    TypedWidgetInstanceCodec.encode(
                        TypedWidgetInstance(
                            0,
                            BatteryWidgetReceiver.DEFAULT_PRODUCT_ID,
                            TypedWidgetConfiguration.Battery(requested),
                        ),
                    ),
                )
            },
        )

        val saved = withTimeoutOrNull(5_000) {
            while (true) {
                val instance = repository.get(appWidgetId)
                if (instance?.configuration == TypedWidgetConfiguration.Battery(requested)) return@withTimeoutOrNull instance
                delay(25)
            }
            null
        }
        assertNotNull(saved)
    }

    private suspend fun awaitInstance(
        appWidgetId: Int,
        expectedConfiguration: DigitalClockConfiguration? = null,
    ): WidgetInstance = requireNotNull(awaitInstanceOrNull(appWidgetId, expectedConfiguration)) {
        "Timed out waiting for widget instance $appWidgetId"
    }

    private suspend fun awaitInstanceOrNull(
        appWidgetId: Int,
        expectedConfiguration: DigitalClockConfiguration? = null,
    ): WidgetInstance? = withTimeoutOrNull(5_000) {
        while (true) {
            val instance = repository.get(appWidgetId)
            if (instance != null && (expectedConfiguration == null || instance.configuration == expectedConfiguration)) {
                return@withTimeoutOrNull instance
            }
            delay(25)
        }
        null
    }

    private fun obviousConfiguration(): DigitalClockConfiguration = DigitalClockConfiguration(
        content = ClockContentConfiguration(),
        date = ClockDateConfiguration(),
        typography = WidgetTypographyConfiguration(
            timeFontFamily = ClockFontFamily.ELEGANT,
            dateFontFamily = ClockFontFamily.DIGITAL,
            timeScale = 1.45f,
            dateScale = 0.62f,
        ),
    )
}
