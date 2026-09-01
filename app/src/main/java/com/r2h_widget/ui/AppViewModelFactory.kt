package com.r2h_widget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.r2h_widget.data.AppDependencies

class AppViewModelFactory(
    private val dependencies: AppDependencies,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) ->
            MainViewModel(dependencies.favoritesRepository) as T

        modelClass.isAssignableFrom(WidgetDetailsViewModel::class.java) ->
            WidgetDetailsViewModel(
                favoritesRepository = dependencies.favoritesRepository,
                widgetInstanceRepository = dependencies.widgetInstanceRepository,
                typedWidgetInstanceRepository = dependencies.typedWidgetInstanceRepository,
                presetRepository = dependencies.widgetPresetRepository,
                customizationRepository = dependencies.customizationRepository,
            ) as T

        modelClass.isAssignableFrom(com.r2h_widget.customization.CustomizationViewModel::class.java) ->
            com.r2h_widget.customization.CustomizationViewModel(
                context = dependencies.applicationContext,
                repository = dependencies.customizationRepository,
            ) as T

        else -> error("Unsupported ViewModel: ${modelClass.name}")
    }
}
