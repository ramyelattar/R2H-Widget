package com.r2h_widget.widget.clock

import com.r2h_widget.widget.common.WidgetSizeClass
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetSizeResolverTest {
    @Test
    fun mapsLauncherDimensionsToResponsiveSizeClasses() {
        assertEquals(WidgetSizeClass.COMPACT, WidgetSizeResolver.fromDp(110, 50))
        assertEquals(WidgetSizeClass.MEDIUM, WidgetSizeResolver.fromDp(220, 100))
        assertEquals(WidgetSizeClass.EXPANDED, WidgetSizeResolver.fromDp(320, 160))
    }

    @Test
    fun usesIndependentScalesForEachSizeClass() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(),
            date = ClockDateConfiguration(),
            responsive = ResponsiveWidgetConfiguration(
                compactScale = 0.7f,
                mediumScale = 1f,
                expandedScale = 1.3f,
            ),
        )

        assertEquals(0.7f, WidgetSizeResolver.scaleFor(WidgetSizeClass.COMPACT, configuration))
        assertEquals(1.3f, WidgetSizeResolver.scaleFor(WidgetSizeClass.EXPANDED, configuration))
    }
}
