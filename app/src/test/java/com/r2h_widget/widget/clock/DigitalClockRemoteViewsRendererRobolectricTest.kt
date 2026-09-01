package com.r2h_widget.widget.clock

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import com.r2h_widget.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DigitalClockRemoteViewsRendererRobolectricTest {
    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun mediumRemoteViews_useSharedClockTypographyBaseForDateSize() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(),
            date = ClockDateConfiguration(),
            typography = WidgetTypographyConfiguration(
                timeScale = 1f,
                dateScale = 0.34f,
            ),
            responsive = ResponsiveWidgetConfiguration(mediumScale = 1f),
        )
        val root = render(configuration)
        val clock = root.requireView<TextClock>(R.id.widget_clock)
        val date = root.requireView<TextClock>(R.id.widget_date)
        val weekday = root.requireView<TextClock>(R.id.widget_weekday)

        assertEquals(38f, clock.textSizeInSp(), 0.01f)
        assertEquals(12.92f, date.textSizeInSp(), 0.01f)
        assertEquals(12.92f, weekday.textSizeInSp(), 0.01f)
    }

    @Test
    fun timeAlignment_followsTimeEditorContentAlignment() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(alignment = ClockTimeAlignment.START),
            date = ClockDateConfiguration(),
            layout = WidgetLayoutConfiguration(alignment = ClockTimeAlignment.CENTER),
        )
        val clock = render(configuration).requireView<TextClock>(R.id.widget_clock)

        assertEquals("START", horizontalGravityName(clock.gravity))
    }

    @Test
    fun dateAlignment_isIndependentFromTimeAlignment() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(alignment = ClockTimeAlignment.CENTER),
            date = ClockDateConfiguration(alignment = ClockDateAlignment.END),
            layout = WidgetLayoutConfiguration(alignment = ClockTimeAlignment.CENTER),
        )
        val dateRow = render(configuration).requireView<LinearLayout>(R.id.widget_date_row)

        assertEquals("END", horizontalGravityName(dateRow.gravity))
    }

    @Test
    fun dateOpacity_changesRenderedDateTextAlpha() {
        val configuration = DigitalClockConfiguration(
            content = ClockContentConfiguration(),
            date = ClockDateConfiguration(),
            typography = WidgetTypographyConfiguration(
                dateColorHex = "#FFFFFF",
                dateOpacity = 0.20f,
            ),
        )
        val date = render(configuration).requireView<TextClock>(R.id.widget_date)

        assertEquals(51, android.graphics.Color.alpha(date.currentTextColor))
    }

    @Test
    fun timeFontFamily_changesRenderedTypeface() {
        val system = render(configurationWithFont(ClockFontFamily.SYSTEM))
            .requireView<TextClock>(R.id.widget_clock)
        val mono = render(configurationWithFont(ClockFontFamily.MONO))
            .requireView<TextClock>(R.id.widget_clock)

        assertNotEquals(system.typeface, mono.typeface)
    }

    @Test
    fun timeFontFamily_systemDiffersFromRounded() {
        assertTimeFontsDistinct(ClockFontFamily.SYSTEM, ClockFontFamily.ROUNDED)
    }

    @Test
    fun timeFontFamily_systemDiffersFromCondensed() {
        assertTimeFontsDistinct(ClockFontFamily.SYSTEM, ClockFontFamily.CONDENSED)
    }

    @Test
    fun timeFontFamily_systemDiffersFromElegant() {
        assertTimeFontsDistinct(ClockFontFamily.SYSTEM, ClockFontFamily.ELEGANT)
    }

    @Test
    fun timeFontFamily_systemDiffersFromDigital() {
        assertTimeFontsDistinct(ClockFontFamily.SYSTEM, ClockFontFamily.DIGITAL)
    }

    @Test
    fun timeFontFamily_roundedDiffersFromCondensed() {
        assertTimeFontsDistinct(ClockFontFamily.ROUNDED, ClockFontFamily.CONDENSED)
    }

    @Test
    fun timeFontFamily_monoDiffersFromDigital() {
        assertTimeFontsDistinct(ClockFontFamily.MONO, ClockFontFamily.DIGITAL)
    }

    @Test
    fun dateFontFamily_isIndependentForBottomDateViews() {
        val root = render(configurationWithFonts(ClockFontFamily.SYSTEM, ClockFontFamily.MONO))
        val clock = root.requireView<TextClock>(R.id.widget_clock)
        val date = root.requireView<TextClock>(R.id.widget_date)
        val weekday = root.requireView<TextClock>(R.id.widget_weekday)

        assertNotEquals(clock.typeface, date.typeface)
        assertNotEquals(clock.typeface, weekday.typeface)
    }

    @Test
    fun dateFontFamily_isIndependentForTopDateViews() {
        val root = render(
            configurationWithFonts(
                timeFont = ClockFontFamily.SYSTEM,
                dateFont = ClockFontFamily.MONO,
                placement = ClockDatePlacement.TOP,
            ),
        )
        val clock = root.requireView<TextClock>(R.id.widget_clock)
        val date = root.requireView<TextClock>(R.id.widget_date_top)
        val weekday = root.requireView<TextClock>(R.id.widget_weekday_top)

        assertNotEquals(clock.typeface, date.typeface)
        assertNotEquals(clock.typeface, weekday.typeface)
    }

    @Test
    fun dateFontFamily_elegantDiffersFromSystem() {
        val systemDate = render(
            configurationWithFonts(ClockFontFamily.SYSTEM, ClockFontFamily.SYSTEM),
        ).requireView<TextClock>(R.id.widget_date)
        val elegantDate = render(
            configurationWithFonts(ClockFontFamily.SYSTEM, ClockFontFamily.ELEGANT),
        ).requireView<TextClock>(R.id.widget_date)

        assertNotEquals(systemDate.typeface, elegantDate.typeface)
    }

    @Test
    fun timeDateGap_changesMeasuredDistanceBetweenClockAndDateRow() {
        val gap0 = measure(geometryConfiguration(timeDateGapDp = 0f))
        val gap20 = measure(geometryConfiguration(timeDateGapDp = 20f))

        val renderedGapChangeDp = (gap20.dateRow.top - gap20.clock.bottom) / density -
            (gap0.dateRow.top - gap0.clock.bottom) / density

        assertEquals(20f, renderedGapChangeDp, 1f)
    }

    @Test
    fun topDatePlacement_timeDateGap_changesMeasuredDistanceBetweenDateAndClock() {
        val gap0 = measure(
            geometryConfiguration(
                datePlacement = ClockDatePlacement.TOP,
                timeDateGapDp = 0f,
            ),
        )
        val gap20 = measure(
            geometryConfiguration(
                datePlacement = ClockDatePlacement.TOP,
                timeDateGapDp = 20f,
            ),
        )

        val renderedGapChangeDp = (gap20.clock.top - gap20.dateRow.bottom) / density -
            (gap0.clock.top - gap0.dateRow.bottom) / density

        assertEquals(20f, renderedGapChangeDp, 1f)
    }

    @Test
    fun horizontalOffset_changesMeasuredCompositionXPosition() {
        val baseline = measure(geometryConfiguration(horizontalOffsetDp = 0f))
        val shifted = measure(geometryConfiguration(horizontalOffsetDp = 15f))

        val renderedOffsetDp = shifted.horizontalAnchorPx / density - baseline.horizontalAnchorPx / density

        assertEquals(15f, renderedOffsetDp, 1f)
    }

    @Test
    fun verticalOffset_changesMeasuredCompositionYPosition() {
        val baseline = measure(geometryConfiguration(verticalOffsetDp = 0f))
        val shifted = measure(geometryConfiguration(verticalOffsetDp = -10f))

        val renderedOffsetDp = shifted.verticalAnchorPx / density - baseline.verticalAnchorPx / density

        assertEquals(-10f, renderedOffsetDp, 1f)
    }

    @Test
    fun geometryControls_changeCombinedRenderedGeometry() {
        val baseline = measure(
            geometryConfiguration(
                contentPaddingDp = 8f,
                timeDateGapDp = 0f,
                horizontalOffsetDp = 0f,
                verticalOffsetDp = 0f,
            ),
        )
        val extreme = measure(
            geometryConfiguration(
                contentPaddingDp = 8f,
                timeDateGapDp = 20f,
                horizontalOffsetDp = 15f,
                verticalOffsetDp = -10f,
            ),
        )

        val horizontalChangeDp = extreme.horizontalAnchorPx / density - baseline.horizontalAnchorPx / density
        val verticalChangeDp = extreme.verticalAnchorPx / density - baseline.verticalAnchorPx / density
        val gapChangeDp = (extreme.dateRow.top - extreme.clock.bottom) / density -
            (baseline.dateRow.top - baseline.clock.bottom) / density

        assertEquals(15f, horizontalChangeDp, 1f)
        assertEquals(-10f, verticalChangeDp, 1f)
        assertEquals(20f, gapChangeDp, 1f)
    }

    private fun configurationWithFont(font: ClockFontFamily): DigitalClockConfiguration =
        DigitalClockConfiguration(
            content = ClockContentConfiguration(),
            date = ClockDateConfiguration(),
            typography = WidgetTypographyConfiguration(timeFontFamily = font),
        )

    private fun assertTimeFontsDistinct(first: ClockFontFamily, second: ClockFontFamily) {
        val firstTypeface = render(configurationWithFont(first)).requireView<TextClock>(R.id.widget_clock).typeface
        val secondTypeface = render(configurationWithFont(second)).requireView<TextClock>(R.id.widget_clock).typeface

        assertNotEquals("$first and $second must render different typefaces", firstTypeface, secondTypeface)
    }

    private fun configurationWithFonts(
        timeFont: ClockFontFamily,
        dateFont: ClockFontFamily,
        placement: ClockDatePlacement = ClockDatePlacement.BOTTOM,
    ): DigitalClockConfiguration = DigitalClockConfiguration(
        content = ClockContentConfiguration(),
        date = ClockDateConfiguration(
            placement = placement,
            weekdayStyle = ClockWeekdayStyle.SHORT,
        ),
        typography = WidgetTypographyConfiguration(
            timeFontFamily = timeFont,
            dateFontFamily = dateFont,
        ),
    )

    private fun render(configuration: DigitalClockConfiguration): View {
        val remoteViews = DigitalClockRemoteViewsRenderer.render(context, configuration)
        val parent = FrameLayout(context)
        return remoteViews.apply(context, parent)
    }

    private fun measure(configuration: DigitalClockConfiguration): MeasuredWidget {
        val root = render(configuration)
        val widthPx = (320f * density).roundToInt()
        val heightPx = (180f * density).roundToInt()
        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
        root.measure(widthSpec, heightSpec)
        root.layout(0, 0, widthPx, heightPx)
        val content = root.requireView<View>(R.id.widget_content)
        val dateRowId = if (configuration.date.placement == ClockDatePlacement.TOP) {
            R.id.widget_date_row_top
        } else {
            R.id.widget_date_row
        }
        return MeasuredWidget(
            clock = root.requireView(R.id.widget_clock),
            dateRow = root.requireView(dateRowId),
            horizontalAnchorPx = content.x,
            verticalAnchorPx = content.y,
        )
    }

    private fun geometryConfiguration(
        contentPaddingDp: Float = 16f,
        timeDateGapDp: Float = 6f,
        horizontalOffsetDp: Float = 0f,
        verticalOffsetDp: Float = 0f,
        datePlacement: ClockDatePlacement = ClockDatePlacement.BOTTOM,
    ): DigitalClockConfiguration = DigitalClockConfiguration(
        content = ClockContentConfiguration(),
        date = ClockDateConfiguration(placement = datePlacement),
        layout = WidgetLayoutConfiguration(
            contentPaddingDp = contentPaddingDp,
            timeDateGapDp = timeDateGapDp,
            horizontalOffsetDp = horizontalOffsetDp,
            verticalOffsetDp = verticalOffsetDp,
        ),
    )

    private data class MeasuredWidget(
        val clock: View,
        val dateRow: View,
        val horizontalAnchorPx: Float,
        val verticalAnchorPx: Float,
    )

    private val density: Float
        get() = context.resources.displayMetrics.density
}

private inline fun <reified T : View> View.requireView(id: Int): T =
    (findViewById(id) as? T)
        ?: error("Expected ${T::class.simpleName} for view id $id")

private fun TextView.textSizeInSp(): Float = textSize / resources.displayMetrics.scaledDensity

private fun horizontalGravityName(gravity: Int): String = when (
    Gravity.getAbsoluteGravity(gravity, View.LAYOUT_DIRECTION_LTR) and Gravity.HORIZONTAL_GRAVITY_MASK
) {
    Gravity.LEFT -> "START"
    Gravity.RIGHT -> "END"
    Gravity.CENTER_HORIZONTAL -> "CENTER"
    else -> "UNSPECIFIED"
}
