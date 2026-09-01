package com.r2h_widget.widget.analog

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class AnalogClockPainterTest {
    @Test
    fun handAngles_mapNoonToTwelveOClock() {
        val noon = fixedTime(hourOfDay = 12, minute = 0, second = 0)

        val angles = AnalogClockPainter.handAngles(noon)

        assertEquals(0f, angles.hourDegrees, 0.01f)
        assertEquals(0f, angles.minuteDegrees, 0.01f)
        assertEquals(0f, angles.secondDegrees, 0.01f)
    }

    @Test
    fun handAngles_mapThreeThirtyToQuarterPositions() {
        val time = fixedTime(hourOfDay = 3, minute = 30, second = 0)

        val angles = AnalogClockPainter.handAngles(time)

        assertEquals(105f, angles.hourDegrees, 0.01f)
        assertEquals(180f, angles.minuteDegrees, 0.01f)
    }

    @Test
    fun handAngles_moveContinuouslyWithMinutesAndSeconds() {
        val exact = fixedTime(hourOfDay = 6, minute = 0, second = 0)
        val later = fixedTime(hourOfDay = 6, minute = 12, second = 0)

        val exactAngles = AnalogClockPainter.handAngles(exact)
        val laterAngles = AnalogClockPainter.handAngles(later)

        assertEquals(180f, exactAngles.hourDegrees, 0.01f)
        // Twelve minutes past the hour moves the hour hand by 12 / 60 * 30° = 6°.
        assertEquals(186f, laterAngles.hourDegrees, 0.01f)
        assertEquals(72f, laterAngles.minuteDegrees, 0.01f)
    }

    @Test
    fun handAngles_secondHandCoversTheFullMinute() {
        val at15 = fixedTime(hourOfDay = 1, minute = 1, second = 15)
        val at45 = fixedTime(hourOfDay = 1, minute = 1, second = 45)

        assertEquals(90f, AnalogClockPainter.handAngles(at15).secondDegrees, 0.01f)
        assertEquals(270f, AnalogClockPainter.handAngles(at45).secondDegrees, 0.01f)
    }

    /** The painter reads wall-clock time in the device default zone, so build fixtures in it. */
    private fun fixedTime(hourOfDay: Int, minute: Int, second: Int): Long =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            clear()
            set(2026, Calendar.AUGUST, 17, hourOfDay, minute, second)
        }.timeInMillis
}
