package com.r2h_widget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.r2h_widget.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.r2h_widget.ui.theme.AppSpacing
import com.r2h_widget.ui.theme.GlassSurface
import com.r2h_widget.ui.theme.TextPrimary
import com.r2h_widget.ui.theme.TextSecondary
import com.r2h_widget.ui.theme.VoidBlack
import com.r2h_widget.customization.CustomizationAction
import com.r2h_widget.customization.CustomizationUiState
import com.r2h_widget.customization.CustomizationViewModel
import com.r2h_widget.ui.customization.IconPacksScreen
import com.r2h_widget.ui.customization.ThemesScreen
import com.r2h_widget.ui.customization.WallpapersScreen
import com.r2h_widget.ui.widgets.WidgetDetailsScreen
import com.r2h_widget.ui.widgets.WidgetGalleryScreen
import com.r2h_widget.widget.analog.AnalogClockConfiguration
import com.r2h_widget.widget.battery.BatteryWidgetConfiguration
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import com.r2h_widget.widget.pinning.WidgetPinningController
import com.r2h_widget.widget.pinning.WidgetPinningResult
import com.r2h_widget.widget.search.SearchWidgetConfiguration
import com.r2h_widget.widget.music.MusicWidgetConfiguration
import com.r2h_widget.widget.weather.WeatherWidgetConfiguration

@Composable
fun R2HWidgetApp(
    viewModel: MainViewModel,
    detailsViewModel: WidgetDetailsViewModel,
    customizationViewModel: CustomizationViewModel,
    pinningController: WidgetPinningController,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detailsState by detailsViewModel.uiState.collectAsStateWithLifecycle()
    val customizationState by customizationViewModel.uiState.collectAsStateWithLifecycle()

    val route = uiState.route
    if (route is AppRoute.WidgetDetails) {
        androidx.compose.runtime.LaunchedEffect(route.widgetId, route.appWidgetId) {
            detailsViewModel.openWidget(route.widgetId, route.appWidgetId)
        }
    }

    R2HWidgetAppContent(
        uiState = uiState,
        detailsState = detailsState,
        customizationState = customizationState,
        onAction = viewModel::onAction,
        onDetailsAction = detailsViewModel::onAction,
        onCustomizationAction = customizationViewModel::onAction,
        onAddWidget = { productId, configuration ->
            pinningController.requestDigitalClockPin(productId, configuration)
        },
        onSaveWidget = { appWidgetId, productId, configuration ->
            pinningController.updateDigitalClock(appWidgetId, productId, configuration)
        },
        onAddAnalogWidget = { productId, configuration ->
            pinningController.requestAnalogClockPin(productId, configuration)
        },
        onSaveAnalogWidget = { appWidgetId, productId, configuration ->
            pinningController.updateAnalogClock(appWidgetId, productId, configuration)
        },
        onAddBatteryWidget = { productId, configuration ->
            pinningController.requestBatteryPin(productId, configuration)
        },
        onSaveBatteryWidget = { appWidgetId, productId, configuration ->
            pinningController.updateBattery(appWidgetId, productId, configuration)
        },
        onAddWeatherWidget = { productId, configuration ->
            pinningController.requestWeatherPin(productId, configuration)
        },
        onSaveWeatherWidget = { appWidgetId, productId, configuration ->
            pinningController.updateWeather(appWidgetId, productId, configuration)
        },
        onAddSearchWidget = { productId, configuration ->
            pinningController.requestSearchPin(productId, configuration)
        },
        onSaveSearchWidget = { appWidgetId, productId, configuration ->
            pinningController.updateSearch(appWidgetId, productId, configuration)
        },
        onAddMusicWidget = { productId, configuration ->
            pinningController.requestMusicPin(productId, configuration)
        },
        onSaveMusicWidget = { appWidgetId, productId, configuration ->
            pinningController.updateMusic(appWidgetId, productId, configuration)
        },
    )
}

@Composable
fun R2HWidgetAppContent(
    uiState: AppUiState,
    detailsState: WidgetDetailsUiState = WidgetDetailsUiState(),
    customizationState: CustomizationUiState = CustomizationUiState(),
    onAction: (AppAction) -> Unit,
    onDetailsAction: (WidgetDetailsAction) -> Unit = {},
    onCustomizationAction: (CustomizationAction) -> Unit = {},
    onAddWidget: (String, DigitalClockConfiguration) -> WidgetPinningResult = { _, _ ->
        WidgetPinningResult.Unsupported
    },
    onSaveWidget: (Int, String, DigitalClockConfiguration) -> WidgetPinningResult = { _, _, _ ->
        WidgetPinningResult.Unsupported
    },
    onAddAnalogWidget: (String, AnalogClockConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveAnalogWidget: (Int, String, AnalogClockConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddBatteryWidget: (String, BatteryWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveBatteryWidget: (Int, String, BatteryWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddWeatherWidget: (String, WeatherWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveWeatherWidget: (Int, String, WeatherWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddSearchWidget: (String, SearchWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveSearchWidget: (Int, String, SearchWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
    onAddMusicWidget: (String, MusicWidgetConfiguration) -> WidgetPinningResult = { _, _ -> WidgetPinningResult.Unsupported },
    onSaveMusicWidget: (Int, String, MusicWidgetConfiguration) -> WidgetPinningResult = { _, _, _ -> WidgetPinningResult.Unsupported },
) {
    when (val route = uiState.route) {
        AppRoute.Home -> Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(R.drawable.app_page_background),
                    contentScale = ContentScale.Crop,
                )
                .safeDrawingPadding(),
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = GlassSurface.copy(alpha = 0.88f)) {
                    AppDestination.entries.forEach { destination ->
                        val selected = uiState.selectedDestination == destination
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onAction(AppAction.SelectDestination(destination)) },
                            icon = {
                                Text(
                                    text = destination.shortLabel,
                                    modifier = Modifier.semantics {
                                        contentDescription = destination.label
                                    },
                                    color = if (selected) TextPrimary else TextSecondary,
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextPrimary,
                                selectedTextColor = TextPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = GlassSurface,
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (uiState.selectedDestination) {
                    AppDestination.WIDGETS -> WidgetGalleryScreen(
                        selectedCategory = uiState.selectedWidgetCategory,
                        favoriteIds = uiState.favoriteIds,
                        onCategorySelected = { category ->
                            onAction(AppAction.SelectWidgetCategory(category))
                        },
                        onWidgetSelected = { widgetId ->
                            onAction(AppAction.OpenWidgetDetails(widgetId))
                        },
                        onToggleFavorite = { widgetId ->
                            onAction(AppAction.ToggleFavorite(widgetId))
                        },
                    )

                    AppDestination.ICONS -> IconPacksScreen(
                        state = customizationState,
                        onAction = onCustomizationAction,
                    )

                    AppDestination.WALLS -> WallpapersScreen(
                        state = customizationState,
                        onAction = onCustomizationAction,
                    )

                    AppDestination.THEMES -> ThemesScreen(
                        state = customizationState,
                        onAction = onCustomizationAction,
                    )
                }
            }
        }

        is AppRoute.WidgetDetails -> WidgetDetailsScreen(
            state = detailsState,
            onBack = { onAction(AppAction.NavigateBack) },
            onAction = onDetailsAction,
            onAddWidget = onAddWidget,
            onSaveWidget = onSaveWidget,
            onAddAnalogWidget = onAddAnalogWidget,
            onSaveAnalogWidget = onSaveAnalogWidget,
            onAddBatteryWidget = onAddBatteryWidget,
            onSaveBatteryWidget = onSaveBatteryWidget,
            onAddWeatherWidget = onAddWeatherWidget,
            onSaveWeatherWidget = onSaveWeatherWidget,
            onAddSearchWidget = onAddSearchWidget,
            onSaveSearchWidget = onSaveSearchWidget,
            onAddMusicWidget = onAddMusicWidget,
            onSaveMusicWidget = onSaveMusicWidget,
        )
    }
}

private val AppDestination.shortLabel: String
    get() = when (this) {
        AppDestination.WIDGETS -> "⌂"
        AppDestination.ICONS -> "✦"
        AppDestination.WALLS -> "◌"
        AppDestination.THEMES -> "◈"
    }

@Composable
private fun EmptyDestination(
    title: String,
    description: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.xxl)
            .safeDrawingPadding(),
    ) {
        Text(text = title, color = TextPrimary)
        Text(text = description, color = TextSecondary)
    }
}










