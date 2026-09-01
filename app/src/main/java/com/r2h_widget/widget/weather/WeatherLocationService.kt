package com.r2h_widget.widget.weather

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.IBinder

class WeatherLocationService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private var listening = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        locationManager = getSystemService(LocationManager::class.java)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (!hasLocationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (listening) return

        if (!locationManager.isLocationEnabled) {
            return
        }

        val provider = when {
            locationManager.allProviders.contains(LocationManager.FUSED_PROVIDER) ->
                LocationManager.FUSED_PROVIDER

            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER

            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER

            else -> return
        }

        val request = LocationRequest.Builder(120_000L)
            .setMinUpdateIntervalMillis(60_000L)
            .setMinUpdateDistanceMeters(250f)
            .setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY)
            .build()

        try {
            locationManager.getLastKnownLocation(provider)?.let {
                onLocationChanged(it)
            }

            locationManager.requestLocationUpdates(
                provider,
                request,
                mainExecutor,
                this,
            )

            listening = true
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    override fun onLocationChanged(location: Location) {
        WeatherLocationStore.save(this, location)
        refreshWeatherWidgets()
    }

    private fun refreshWeatherWidgets() {
        val manager =
            android.appwidget.AppWidgetManager.getInstance(this)

        val component =
            android.content.ComponentName(
                this,
                WeatherWidgetReceiver::class.java,
            )

        val widgetIds =
            manager.getAppWidgetIds(component)

        if (widgetIds.isEmpty()) return

        sendBroadcast(
            android.content.Intent(
                android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            ).apply {
                this.component = component
                putExtra(
                    android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    widgetIds,
                )
            },
        )
    }

    override fun onDestroy() {
        if (listening) {
            try {
                locationManager.removeUpdates(this)
            } catch (_: SecurityException) {
                // Permission may have been revoked while the service was active.
            }

            listening = false
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Weather location",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Keeps weather widgets updated for your current location"
                setShowBadge(false)
            },
        )
    }

    private fun buildNotification(): Notification =
        Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Weather widget active")
            .setContentText("Following your location for weather updates")
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "weather_location_monitor"
        private const val NOTIFICATION_ID = 2501

        fun start(context: Context) {
            context.startForegroundService(
                Intent(context, WeatherLocationService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(
                Intent(context, WeatherLocationService::class.java),
            )
        }
    }
}

