package com.r2h_widget.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.ui.WidgetDetailsUiState
import com.r2h_widget.ui.WidgetStudioMoreSection
import com.r2h_widget.ui.WidgetStudioSection
import com.r2h_widget.ui.theme.R2HWidgetTheme
import com.r2h_widget.widget.pinning.WidgetPinningResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WidgetDetailsScreenRobolectricTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun advancedTypography_doesNotExposeTimeWeightEditor() {
        setAdvancedEditorContent()

        composeRule.onNode(hasScrollToIndexAction(), useUnmergedTree = true).performScrollToIndex(3)
        composeRule.onAllNodesWithText("Time weight", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun advancedTypography_doesNotExposeDateWeightEditor() {
        setAdvancedEditorContent()

        composeRule.onNode(hasScrollToIndexAction(), useUnmergedTree = true).performScrollToIndex(3)
        composeRule.onAllNodesWithText("Date weight", useUnmergedTree = true).assertCountEquals(0)
    }

    private fun setAdvancedEditorContent() {
        composeRule.setContent {
            R2HWidgetTheme {
                Box(Modifier.size(1000.dp, 2000.dp)) {
                    WidgetDetailsScreen(
                        state = WidgetDetailsUiState(
                            widgetId = "digital-clock",
                            definition = WidgetCatalog.findById("digital-clock"),
                            selectedSection = WidgetStudioSection.MORE,
                            selectedMoreSection = WidgetStudioMoreSection.ADVANCED,
                        ),
                        onBack = {},
                        onAction = {},
                        onAddWidget = { _, _ -> WidgetPinningResult.Unsupported },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }
}
