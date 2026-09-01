package com.r2h_widget.data

import com.r2h_widget.widget.clock.ClockAccent
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockDateAlignment
import com.r2h_widget.widget.clock.ClockTimeAlignment
import com.r2h_widget.widget.clock.ClockContentConfiguration
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.ResponsiveWidgetConfiguration
import com.r2h_widget.widget.clock.WidgetLayoutConfiguration
import com.r2h_widget.widget.clock.WidgetTypographyConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetInstanceRepositoryTest {
    @Test
    fun codec_roundTripsProductAndConfiguration() {
        val original = WidgetInstance(
            appWidgetId = 42,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(
                use24HourFormat = true,
                showDate = false,
                showWeekday = true,
                accent = ClockAccent.CYAN,
            ),
        )

        assertEquals(original, WidgetInstanceCodec.decode(42, WidgetInstanceCodec.encode(original)))
    }

    @Test
    fun twoInstancesOfSameProduct_keepIndependentConfigurationAndDeleteCleanly() = runBlocking {
        val repository = InMemoryWidgetInstanceRepository()
        val first = WidgetInstance(
            appWidgetId = 101,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(accent = ClockAccent.VIOLET),
        )
        val second = WidgetInstance(
            appWidgetId = 102,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(
                use24HourFormat = true,
                accent = ClockAccent.MINT,
            ),
        )

        repository.save(first)
        repository.save(second)
        assertEquals(first, repository.get(101))
        assertEquals(second, repository.get(102))

        repository.delete(101)
        assertNull(repository.get(101))
        assertEquals(second, repository.get(102))
    }

    @Test
    fun legacyHashPrefixedValue_migratesToCanonicalProduct() {
        val legacy = "#*!1|clock-05|false|true|true|VIOLET"

        val decoded = requireNotNull(WidgetInstanceCodec.decode(7, legacy))

        assertEquals("digital-clock", decoded.productId)
        assertEquals(true, decoded.configuration.showDate)
        assertEquals(true, decoded.configuration.showWeekday)
        assertEquals(ClockAccent.VIOLET, decoded.configuration.accent)
        assertEquals(true, WidgetInstanceCodec.isCurrent(WidgetInstanceCodec.encode(decoded)))
    }

    @Test
    fun studioConfiguration_roundTripsTypedSections() {
        val original = WidgetInstance(
            appWidgetId = 88,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(
                content = com.r2h_widget.widget.clock.ClockContentConfiguration(
                    use24HourFormat = true,
                    leadingZero = true,
                ),
                date = com.r2h_widget.widget.clock.ClockDateConfiguration(enabled = false),
                background = com.r2h_widget.widget.common.WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.RADIAL_GRADIENT,
                    gradientAngleDegrees = 212f,
                ),
                typography = com.r2h_widget.widget.clock.WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.MONO,
                    timeScale = 1.22f,
                ),
            ),
        )

        assertEquals(original, WidgetInstanceCodec.decode(88, WidgetInstanceCodec.encode(original)))
    }

    @Test
    fun extremeFidelityConfiguration_roundTripsAllRendererFields() {
        val original = WidgetInstance(
            appWidgetId = 99,
            productId = "digital-clock",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(alignment = ClockTimeAlignment.START),
                date = ClockDateConfiguration(alignment = ClockDateAlignment.END),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.DIGITAL,
                    timeFontWeight = 800,
                    timeScale = 1.45f,
                    dateFontFamily = ClockFontFamily.ELEGANT,
                    dateFontWeight = 600,
                    dateScale = 0.62f,
                    dateOpacity = 1f,
                ),
                layout = WidgetLayoutConfiguration(
                    contentPaddingDp = 8f,
                    timeDateGapDp = 20f,
                    horizontalOffsetDp = 15f,
                    verticalOffsetDp = -10f,
                ),
            ),
        )

        assertEquals(original, WidgetInstanceCodec.decode(99, WidgetInstanceCodec.encode(original)))
    }

    @Test
    fun currentFormatWithLegacyProductId_decodesToCanonicalProduct() {
        val encoded = WidgetInstanceCodec.encode(
            WidgetInstance(31, "clock-07", DigitalClockConfiguration(accent = ClockAccent.ROSE)),
        )

        val decoded = requireNotNull(WidgetInstanceCodec.decode(31, encoded))

        assertEquals("digital-clock", decoded.productId)
    }
}
