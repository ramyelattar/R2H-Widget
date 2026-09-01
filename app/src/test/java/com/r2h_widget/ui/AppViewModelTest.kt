package com.r2h_widget.ui

import com.r2h_widget.MainDispatcherRule
import com.r2h_widget.catalog.WidgetCategory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_opensWidgetsAndAllCategory() {
        val viewModel = MainViewModel()

        assertEquals(AppDestination.WIDGETS, viewModel.uiState.value.selectedDestination)
        assertEquals(WidgetCategory.ALL, viewModel.uiState.value.selectedWidgetCategory)
    }

    @Test
    fun selectingDestination_updatesOnlyDestination() {
        val viewModel = MainViewModel()

        viewModel.onAction(AppAction.SelectDestination(AppDestination.ICONS))

        assertEquals(AppDestination.ICONS, viewModel.uiState.value.selectedDestination)
        assertEquals(WidgetCategory.ALL, viewModel.uiState.value.selectedWidgetCategory)
    }

    @Test
    fun selectingWidgetCategory_updatesOnlyCategory() {
        val viewModel = MainViewModel()

        viewModel.onAction(AppAction.SelectWidgetCategory(WidgetCategory.CLOCK))

        assertEquals(AppDestination.WIDGETS, viewModel.uiState.value.selectedDestination)
        assertEquals(WidgetCategory.CLOCK, viewModel.uiState.value.selectedWidgetCategory)
    }

    @Test
    fun openingWidgetDetails_usesStableWidgetId() {
        val viewModel = MainViewModel()

        viewModel.onAction(AppAction.OpenWidgetDetails("digital-clock"))

        assertEquals(AppRoute.WidgetDetails("digital-clock"), viewModel.uiState.value.route)
    }
}
