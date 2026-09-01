package com.r2h_widget.widget.battery

import android.content.Intent
import android.os.BatteryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BatteryStateReaderTest {
    @Test
    fun mapsPlatformBatteryIntentToWidgetState() {
        val state = BatteryStateReader.fromIntent(
            Intent(Intent.ACTION_BATTERY_CHANGED)
                .putExtra(BatteryManager.EXTRA_LEVEL, 12)
                .putExtra(BatteryManager.EXTRA_SCALE, 100)
                .putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
                .putExtra(BatteryManager.EXTRA_PLUGGED, 0),
        )

        assertEquals(12, state.levelPercent)
        assertEquals(BatteryPlugSource.UNKNOWN, state.plugSource)
        assertTrue(state.isLow)
    }
}
