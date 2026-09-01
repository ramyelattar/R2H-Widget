package com.r2h_widget.ui

import androidx.lifecycle.ViewModel
import com.r2h_widget.catalog.WidgetCategory
import com.r2h_widget.data.FavoritesRepository
import com.r2h_widget.data.InMemoryFavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

enum class AppDestination(val label: String) {
    WIDGETS("Widgets"),
    ICONS("Icons"),
    WALLS("Walls"),
    THEMES("Themes"),
}

sealed interface AppRoute {
    data object Home : AppRoute
    data class WidgetDetails(val widgetId: String, val appWidgetId: Int? = null) : AppRoute
}

data class AppUiState(
    val route: AppRoute = AppRoute.Home,
    val selectedDestination: AppDestination = AppDestination.WIDGETS,
    val selectedWidgetCategory: WidgetCategory = WidgetCategory.ALL,
    val favoriteIds: Set<String> = emptySet(),
)

sealed interface AppAction {
    data class SelectDestination(val destination: AppDestination) : AppAction
    data class SelectWidgetCategory(val category: WidgetCategory) : AppAction
    data class OpenWidgetDetails(val widgetId: String, val appWidgetId: Int? = null) : AppAction
    data object NavigateBack : AppAction
    data class ToggleFavorite(val productId: String) : AppAction
}

class MainViewModel(
    private val favoritesRepository: FavoritesRepository = InMemoryFavoritesRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.favoriteIds.collect { favoriteIds ->
                _uiState.update { it.copy(favoriteIds = favoriteIds) }
            }
        }
    }

    fun onAction(action: AppAction) {
        when (action) {
            is AppAction.SelectDestination -> _uiState.update {
                it.copy(route = AppRoute.Home, selectedDestination = action.destination)
            }

            is AppAction.SelectWidgetCategory -> _uiState.update {
                it.copy(selectedWidgetCategory = action.category)
            }

            is AppAction.OpenWidgetDetails -> _uiState.update {
                it.copy(route = AppRoute.WidgetDetails(action.widgetId, action.appWidgetId))
            }

            AppAction.NavigateBack -> _uiState.update { it.copy(route = AppRoute.Home) }

            is AppAction.ToggleFavorite -> viewModelScope.launch {
                favoritesRepository.toggleFavorite(action.productId)
            }
        }
    }
}
