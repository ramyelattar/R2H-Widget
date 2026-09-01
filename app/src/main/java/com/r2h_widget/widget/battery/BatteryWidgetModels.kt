package com.r2h_widget.widget.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration

/** Arrangement of level, progress, and status inside the battery widget. */
enum class BatteryWidgetLayout {
    /** Big percentage stacked over the progress bar and status line. */
    STACKED,

    /** Percentage and a slim vertical-aware bar side by side. */
    HORIZONTAL,

    /** Just the percentage and a thin bar, for the smallest cells. */
    COMPACT,
}

/**
 * Typed configuration for the single Battery product. Appearance differences that the
 * old preset catalog expressed as separate products live here as controls.
 */
data class BatteryWidgetConfiguration(
    val layout: BatteryWidgetLayout = BatteryWidgetLayout.STACKED,
    val showPercentage: Boolean = true,
    val showChargingState: Boolean = true,
    val showPlugSource: Boolean = false,
    val showIcon: Boolean = true,
    val textScale: Float = 1f,
    val percentColorHex: String = "#F4F2FA",
    val statusColorHex: String = "#A7A4B5",
    val progressColorHex: String = "#34D399",
    val lowBatteryColorHex: String = "#FB7185",
    val chargingColorHex: String = "#34D399",
    val contentPaddingDp: Float = 16f,
    val alignStart: Boolean = false,
    val background: WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(),
    val action: WidgetActionConfiguration = WidgetActionConfiguration(),
) {
    /** Color the level indicator should use for the current state. */
    fun indicatorColorHex(state: BatteryWidgetState): String = when {
        state.isCharging -> chargingColorHex
        state.isLow -> lowBatteryColorHex
        else -> progressColorHex
    }
}

enum class BatteryPlugSource {
    AC,
    USB,
    WIRELESS,
    UNKNOWN,
}

data class BatteryWidgetState(
    val levelPercent: Int,
    val isCharging: Boolean,
    val plugSource: BatteryPlugSource,
    val isLow: Boolean,
)

object BatteryStateReader {
    fun read(context: Context): BatteryWidgetState {
        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return fromIntent(intent)
    }

    fun fromIntent(intent: Intent?): BatteryWidgetState {
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.coerceAtLeast(1) ?: 100
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val levelPercent = ((level * 100f) / scale).toInt().coerceIn(0, 100)
        return BatteryWidgetState(
            levelPercent = levelPercent,
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL,
            plugSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> BatteryPlugSource.AC
                BatteryManager.BATTERY_PLUGGED_USB -> BatteryPlugSource.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryPlugSource.WIRELESS
                else -> BatteryPlugSource.UNKNOWN
            },
            isLow = levelPercent <= 15,
        )
    }
}
