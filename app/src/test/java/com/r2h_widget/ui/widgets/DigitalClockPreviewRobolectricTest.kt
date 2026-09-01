package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.widget.clock.ClockDateConfiguration
import com.r2h_widget.widget.clock.ClockDateFormatPreset
import com.r2h_widget.widget.clock.ClockTimeAlignment
import com.r2h_widget.widget.clock.ClockWeekdayStyle
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.clock.WidgetLayoutConfiguration
import com.r2h_widget.widget.common.WidgetSizeClass
import com.r2h_widget.widget.clock.WidgetTypographyConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DigitalClockPreviewRobolectricTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dateScale_changesRenderedDateGeometry() {
        setSideBySide(
            previewConfiguration(dateScale = 0.18f),
            previewConfiguration(dateScale = 0.62f),
        )

        val panels = measuredPanels()
        val smallDateHeight = panels[0].date.height
        val largeDateHeight = panels[1].date.height

        assertTrue(
            "dateScale=.62 should render a materially larger date than .18; " +
                "small=${smallDateHeight}px large=${largeDateHeight}px",
            largeDateHeight > smallDateHeight,
        )
    }

    @Test
    fun timeAlignment_followsContentAlignmentInPreview() {
        setSingle(
            previewConfiguration(
                contentAlignment = ClockTimeAlignment.START,
                layoutAlignment = ClockTimeAlignment.CENTER,
                dateEnabled = false,
            ),
        )

        val timeLeft = measuredPanels(singlePanel = true).single().time.left

        assertEquals(
            "START-aligned time should begin at the 16dp content inset",
            16f,
            timeLeft,
            2f,
        )
    }

    @Test
    fun horizontalOffset_movesPreviewComposition() {
        setSideBySide(
            previewConfiguration(horizontalOffsetDp = 0f, dateEnabled = false),
            previewConfiguration(horizontalOffsetDp = 15f, dateEnabled = false),
        )

        val panels = measuredPanels()
        val renderedOffset = (panels[1].time.left - PANEL_WIDTH_DP) - panels[0].time.left

        assertEquals(15f, renderedOffset, 1f)
    }

    @Test
    fun verticalOffset_movesPreviewComposition() {
        setSideBySide(
            previewConfiguration(verticalOffsetDp = 0f, dateEnabled = false),
            previewConfiguration(verticalOffsetDp = -10f, dateEnabled = false),
        )

        val panels = measuredPanels()
        val renderedOffset = panels[1].time.top - panels[0].time.top

        assertEquals(-10f, renderedOffset, 1f)
    }

    @Test
    fun mediumPreview_usesSharedTimeSizeContract() {
        setSingle(
            previewConfiguration(
                dateEnabled = false,
                timeScale = 1f,
            ),
        )

        val timeHeight = measuredPanels(singlePanel = true).single().time.height

        assertEquals(40f, timeHeight, 1f)
    }

    @Test
    fun timeDateGap_changesMeasuredPreviewDistance() {
        setSideBySide(
            previewConfiguration(timeDateGapDp = 0f),
            previewConfiguration(timeDateGapDp = 20f),
        )

        val panels = measuredPanels()
        val gap0 = panels[0].date.top - panels[0].time.bottom
        val gap20 = panels[1].date.top - panels[1].time.bottom

        assertEquals(20f, gap20 - gap0, 1f)
    }

    private fun setSingle(configuration: DigitalClockConfiguration) {
        composeRule.setContent {
            deterministicDensity {
                PreviewPanel(configuration)
            }
        }
        composeRule.waitForIdle()
    }

    private fun setSideBySide(
        left: DigitalClockConfiguration,
        right: DigitalClockConfiguration,
    ) {
        composeRule.setContent {
            deterministicDensity {
                Row(
                    modifier = Modifier
                        .width((PANEL_WIDTH_DP * 2).dp)
                        .height(PANEL_HEIGHT_DP.dp),
                ) {
                    PreviewPanel(left)
                    PreviewPanel(right)
                }
            }
        }
        composeRule.waitForIdle()
    }

    @androidx.compose.runtime.Composable
    private fun deterministicDensity(content: @androidx.compose.runtime.Composable () -> Unit) {
        CompositionLocalProvider(LocalDensity provides Density(1f, 1f), content = content)
    }

    @androidx.compose.runtime.Composable
    private fun PreviewPanel(configuration: DigitalClockConfiguration) {
        Box(modifier = Modifier.size(PANEL_WIDTH_DP.dp, PANEL_HEIGHT_DP.dp)) {
            DigitalClockPreview(
                definition = definition,
                modifier = Modifier.fillMaxSize(),
                accent = PREVIEW_ACCENT,
                configuration = configuration,
                sizeClass = WidgetSizeClass.MEDIUM,
                live = false,
            )
        }
    }

    private fun measuredPanels(singlePanel: Boolean = false): List<PreviewPanelGeometry> {
        val root = composeRule.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val textNodes = root
            .flatten()
            .filter { it.config.contains(SemanticsProperties.Text) }
        val groups = textNodes
            .groupBy { if (it.boundsInRoot.center.x < PANEL_WIDTH_DP) 0 else 1 }
            .toSortedMap()

        if (singlePanel) {
            require(groups.size == 1) { "Expected one preview panel, found groups=$groups" }
        } else {
            require(groups.keys == setOf(0, 1)) {
                "Expected two preview panels, found root=${root.boundsInRoot} " +
                    "bounds=${textNodes.joinToString { it.boundsInRoot.toString() }}"
            }
        }

        return groups.values.map { nodes ->
            val ordered = nodes.sortedBy { it.boundsInRoot.top }
            PreviewPanelGeometry(
                time = ordered.first().boundsInRoot,
                date = ordered.last().boundsInRoot,
            )
        }
    }

    private fun SemanticsNode.flatten(): List<SemanticsNode> =
        listOf(this) + children.flatMap { it.flatten() }

    private fun previewConfiguration(
        dateScale: Float = 0.34f,
        timeScale: Float = 1f,
        dateEnabled: Boolean = true,
        timeDateGapDp: Float = 6f,
        horizontalOffsetDp: Float = 0f,
        verticalOffsetDp: Float = 0f,
        contentAlignment: ClockTimeAlignment = ClockTimeAlignment.CENTER,
        layoutAlignment: ClockTimeAlignment = ClockTimeAlignment.CENTER,
    ): DigitalClockConfiguration = DigitalClockConfiguration(
        content = com.r2h_widget.widget.clock.ClockContentConfiguration(
            use24HourFormat = true,
            showAmPm = false,
            leadingZero = true,
            alignment = contentAlignment,
        ),
        date = ClockDateConfiguration(
            enabled = dateEnabled,
            weekdayStyle = ClockWeekdayStyle.HIDDEN,
            formatPreset = ClockDateFormatPreset.DAY_MONTH,
        ),
        typography = WidgetTypographyConfiguration(
            timeScale = timeScale,
            dateScale = dateScale,
        ),
        layout = WidgetLayoutConfiguration(
            contentPaddingDp = 16f,
            horizontalOffsetDp = horizontalOffsetDp,
            verticalOffsetDp = verticalOffsetDp,
            timeDateGapDp = timeDateGapDp,
            alignment = layoutAlignment,
        ),
    )

    private data class PreviewPanelGeometry(
        val time: Rect,
        val date: Rect,
    )

    private val definition = requireNotNull(WidgetCatalog.findById("clock-01"))

    private companion object {
        const val PANEL_WIDTH_DP = 160f
        const val PANEL_HEIGHT_DP = 180f
        val PREVIEW_ACCENT = androidx.compose.ui.graphics.Color(0xFFA78BFA)
    }
}
