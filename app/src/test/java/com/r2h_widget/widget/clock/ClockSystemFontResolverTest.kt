package com.r2h_widget.widget.clock

import org.junit.Assert.assertEquals
import org.junit.Test

class ClockSystemFontResolverTest {
    @Test
    fun launcherSafeFamilyNames_matchVerifiedDeviceMapping() {
        assertEquals("sans-serif", ClockSystemFontResolver.familyName(ClockFontFamily.SYSTEM))
        assertEquals("casual", ClockSystemFontResolver.familyName(ClockFontFamily.ROUNDED))
        assertEquals("sans-serif-condensed", ClockSystemFontResolver.familyName(ClockFontFamily.CONDENSED))
        assertEquals("monospace", ClockSystemFontResolver.familyName(ClockFontFamily.MONO))
        assertEquals("cursive", ClockSystemFontResolver.familyName(ClockFontFamily.ELEGANT))
        assertEquals("serif-monospace", ClockSystemFontResolver.familyName(ClockFontFamily.DIGITAL))
    }
}
