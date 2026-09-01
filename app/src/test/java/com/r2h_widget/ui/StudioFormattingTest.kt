package com.r2h_widget.ui

import com.r2h_widget.ui.widgets.formatStudioNumber
import com.r2h_widget.ui.widgets.formatStudioPercent
import com.r2h_widget.ui.widgets.formatStudioScale
import org.junit.Assert.assertEquals
import org.junit.Test

class StudioFormattingTest {
    @Test
    fun numericValues_useHumanReadableUnits() {
        assertEquals("20", formatStudioNumber(19.96f))
        assertEquals("+7", formatStudioNumber(6.91f, signed = true))
        assertEquals("-7", formatStudioNumber(-6.62f, signed = true))
        assertEquals("14%", formatStudioPercent(0.1355f))
        assertEquals("116%", formatStudioScale(1.16f))
    }
}
