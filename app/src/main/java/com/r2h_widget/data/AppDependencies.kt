package com.r2h_widget.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.r2h_widget.catalog.WidgetCatalog

private val Context.r2hDataStore by preferencesDataStore(name = "r2h_widget_data")

class AppDependencies(context: Context) {
    val applicationContext: Context = context.applicationContext
    private val appContext = applicationContext
    private val validProductIds = WidgetCatalog.all.map { it.id }.toSet()
    private val dataStore = appContext.r2hDataStore

    val favoritesRepository: FavoritesRepository = DataStoreFavoritesRepository(
        preferencesDataStore = dataStore,
        validProductIds = validProductIds,
    )

    val widgetInstanceRepository: WidgetInstanceRepository = DataStoreWidgetInstanceRepository(dataStore)

    val typedWidgetInstanceRepository: TypedWidgetInstanceRepository =
        DataStoreTypedWidgetInstanceRepository(dataStore)

    val widgetPresetRepository: WidgetPresetRepository = DataStoreWidgetPresetRepository(dataStore)

    val customizationRepository: com.r2h_widget.customization.CustomizationRepository =
        com.r2h_widget.customization.DataStoreCustomizationRepository(dataStore)
}
