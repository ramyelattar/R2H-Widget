package com.r2h_widget.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.r2h_widget.catalog.WidgetCategory
import com.r2h_widget.catalog.WidgetCatalog
import com.r2h_widget.ui.theme.R2HWidgetTheme
import com.r2h_widget.ui.widgets.WidgetDetailsScreen
import com.r2h_widget.widget.pinning.WidgetPinningResult
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingDigitalClockCard_emitsStableIdDetailsAction() {
        var capturedAction: AppAction? = null
        composeRule.setContent {
            R2HWidgetTheme {
                R2HWidgetAppContent(
                    uiState = AppUiState(selectedWidgetCategory = WidgetCategory.CLOCK),
                    onAction = { capturedAction = it },
                )
            }
        }

        composeRule.onNodeWithText("Digital Clock").performClick()

        assertEquals(AppAction.OpenWidgetDetails("digital-clock"), capturedAction)
    }

    @Test
    fun detailsFavoriteAndConfigurationControls_emitActions() {
        val actions = mutableListOf<WidgetDetailsAction>()
        composeRule.setContent {
            R2HWidgetTheme {
                WidgetDetailsScreen(
                    state = WidgetDetailsUiState(
                        widgetId = "digital-clock",
                        definition = WidgetCatalog.findById("digital-clock"),
                    ),
                    onBack = {},
                    onAction = actions::add,
                    onAddWidget = { _, _ -> WidgetPinningResult.Unsupported },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Add to favorites").performClick()
        composeRule.onNodeWithContentDescription("Section Time").performClick()
        composeRule.onNodeWithContentDescription("24-hour format").performClick()

        assertEquals(WidgetDetailsAction.ToggleFavorite, actions[0])
        assertEquals(WidgetDetailsAction.Set24HourFormat(true), actions[1])
    }

    @Test
    fun detailsAddWidget_reportsUnsupportedLauncherState() {
        val actions = mutableListOf<WidgetDetailsAction>()
        composeRule.setContent {
            R2HWidgetTheme {
                WidgetDetailsScreen(
                    state = WidgetDetailsUiState(
                        widgetId = "digital-clock",
                        definition = WidgetCatalog.findById("digital-clock"),
                    ),
                    onBack = {},
                    onAction = actions::add,
                    onAddWidget = { _, _ -> WidgetPinningResult.Unsupported },
                )
            }
        }

        composeRule.onNodeWithText("Add Widget").performClick()

        assertEquals(
            WidgetDetailsAction.PinningCompleted(WidgetPinningResult.Unsupported),
            actions.single(),
        )
    }
}
