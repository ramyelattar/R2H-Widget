package com.r2h_widget.widget.clock

import com.r2h_widget.widget.common.WidgetSizeClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockStyleResolverTest {
    @Test
    fun mediumTypographyUsesCanonicalBaseForTimeAndDate() {
        val configuration = configuration(
            typography = WidgetTypographyConfiguration(
                timeScale = 1f,
                dateScale = 0.34f,
            ),
            responsive = ResponsiveWidgetConfiguration(mediumScale = 1f),
        )

        val resolved = ClockStyleResolver.resolve(configuration, WidgetSizeClass.MEDIUM)

        assertEquals(38f, resolved.timeSizeSp, 0.001f)
        assertEquals(12.92f, resolved.dateSizeSp, 0.001f)
    }

    @Test
    fun changingTimeScaleChangesResolvedTimeSize() {
        val smaller = ClockStyleResolver.resolve(
            configuration(typography = WidgetTypographyConfiguration(timeScale = 0.65f)),
            WidgetSizeClass.MEDIUM,
        )
        val larger = ClockStyleResolver.resolve(
            configuration(typography = WidgetTypographyConfiguration(timeScale = 1.45f)),
            WidgetSizeClass.MEDIUM,
        )

        assertTrue(larger.timeSizeSp > smaller.timeSizeSp)
    }

    @Test
    fun changingDateScaleChangesResolvedDateSize() {
        val smaller = ClockStyleResolver.resolve(
            configuration(typography = WidgetTypographyConfiguration(dateScale = 0.18f)),
            WidgetSizeClass.MEDIUM,
        )
        val larger = ClockStyleResolver.resolve(
            configuration(typography = WidgetTypographyConfiguration(dateScale = 0.62f)),
            WidgetSizeClass.MEDIUM,
        )

        assertTrue(larger.dateSizeSp > smaller.dateSizeSp)
    }

    @Test
    fun responsiveScaleIsAppliedExactlyOnceForEachSizeClass() {
        val configuration = configuration(
            typography = WidgetTypographyConfiguration(timeScale = 1.25f, dateScale = 0.5f),
            responsive = ResponsiveWidgetConfiguration(
                compactScale = 0.8f,
                mediumScale = 1f,
                expandedScale = 1.2f,
            ),
        )

        val compact = ClockStyleResolver.resolve(configuration, WidgetSizeClass.COMPACT)
        val medium = ClockStyleResolver.resolve(configuration, WidgetSizeClass.MEDIUM)
        val expanded = ClockStyleResolver.resolve(configuration, WidgetSizeClass.EXPANDED)

        assertEquals(0.8f, compact.responsiveScale, 0f)
        assertEquals(1f, medium.responsiveScale, 0f)
        assertEquals(1.2f, expanded.responsiveScale, 0f)
        assertEquals(38f * 1.25f * 0.8f, compact.timeSizeSp, 0.001f)
        assertEquals(38f * 0.5f * 0.8f, compact.dateSizeSp, 0.001f)
        assertEquals(38f * 1.25f, medium.timeSizeSp, 0.001f)
        assertEquals(38f * 1.25f * 1.2f, expanded.timeSizeSp, 0.001f)
    }

    @Test
    fun timeAlignmentUsesContentAlignmentAsSourceOfTruth() {
        val resolved = ClockStyleResolver.resolve(
            configuration(
                content = ClockContentConfiguration(alignment = ClockTimeAlignment.START),
                layout = WidgetLayoutConfiguration(alignment = ClockTimeAlignment.CENTER),
            ),
            WidgetSizeClass.MEDIUM,
        )

        assertEquals(ClockTimeAlignment.START, resolved.timeAlignment)
    }

    @Test
    fun dateAlignmentResolvesIndependently() {
        val resolved = ClockStyleResolver.resolve(
            configuration(
                content = ClockContentConfiguration(alignment = ClockTimeAlignment.CENTER),
                date = ClockDateConfiguration(alignment = ClockDateAlignment.END),
                layout = WidgetLayoutConfiguration(alignment = ClockTimeAlignment.CENTER),
            ),
            WidgetSizeClass.MEDIUM,
        )

        assertEquals(ClockDateAlignment.END, resolved.dateAlignment)
    }

    @Test
    fun dateOpacityIsAppliedToResolvedDateColor() {
        val resolved = ClockStyleResolver.resolve(
            configuration(
                typography = WidgetTypographyConfiguration(
                    dateColorHex = "#FFFFFF",
                    dateOpacity = 0.20f,
                ),
            ),
            WidgetSizeClass.MEDIUM,
        )

        assertEquals(51, (resolved.dateColor ushr 24) and 0xFF)
        assertEquals(51, (resolved.weekdayColor ushr 24) and 0xFF)
    }

    @Test
    fun layoutValuesSurviveResolution() {
        val resolved = ClockStyleResolver.resolve(
            configuration(
                layout = WidgetLayoutConfiguration(
                    contentPaddingDp = 8f,
                    horizontalOffsetDp = 15f,
                    verticalOffsetDp = -10f,
                    timeDateGapDp = 20f,
                ),
            ),
            WidgetSizeClass.MEDIUM,
        )

        assertEquals(8f, resolved.contentPaddingDp, 0f)
        assertEquals(20f, resolved.timeDateGapDp, 0f)
        assertEquals(15f, resolved.horizontalOffsetDp, 0f)
        assertEquals(-10f, resolved.verticalOffsetDp, 0f)
    }

    private fun configuration(
        content: ClockContentConfiguration = ClockContentConfiguration(),
        date: ClockDateConfiguration = ClockDateConfiguration(),
        typography: WidgetTypographyConfiguration = WidgetTypographyConfiguration(),
        layout: WidgetLayoutConfiguration = WidgetLayoutConfiguration(),
        responsive: ResponsiveWidgetConfiguration = ResponsiveWidgetConfiguration(),
    ): DigitalClockConfiguration = DigitalClockConfiguration(
        content = content,
        date = date,
        typography = typography,
        layout = layout,
        responsive = responsive,
    )
}
