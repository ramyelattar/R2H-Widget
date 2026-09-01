package com.r2h_widget.widget.analog

import android.widget.FrameLayout
import android.widget.ImageView
import android.content.Context
import com.r2h_widget.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AnalogClockRemoteViewsRendererTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun renderer_paintsBackgroundAndDialIntoTheSharedLayout() {
        val views = AnalogClockRemoteViewsRenderer.render(
            context,
            AnalogClockConfiguration(),
            timeMillis = 1_771_344_000_000L,
        )
        val root = FrameLayout(context)
        val inflated = views.apply(context, root)

        assertNotNull(inflated.findViewById<ImageView>(R.id.analog_widget_background).drawable)
        assertNotNull(inflated.findViewById<ImageView>(R.id.analog_dial).drawable)
    }

    @Test
    fun dateText_followsDateConfiguration() {
        val withDate = AnalogClockConfiguration(
            date = com.r2h_widget.widget.clock.ClockDateConfiguration(
                enabled = true,
                weekdayStyle = com.r2h_widget.widget.clock.ClockWeekdayStyle.SHORT,
                formatPreset = com.r2h_widget.widget.clock.ClockDateFormatPreset.DAY_MONTH,
            ),
        )
        val withoutDate = withDate.copy(
            date = withDate.date.copy(enabled = false),
        )

        assertNotNull(AnalogClockRemoteViewsRenderer.dateTextFor(withDate, 1_771_344_000_000L))
        assertEquals(null, AnalogClockRemoteViewsRenderer.dateTextFor(withoutDate, 1_771_344_000_000L))
    }
}
