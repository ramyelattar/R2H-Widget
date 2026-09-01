package com.r2h_widget.data

import com.r2h_widget.catalog.WidgetCatalog
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface FavoritesRepository {
    val favoriteIds: Flow<Set<String>>

    suspend fun setFavorite(productId: String, favorite: Boolean)

    suspend fun toggleFavorite(productId: String)
}

class InMemoryFavoritesRepository(
    private val validProductIds: Set<String> = WidgetCatalog.all.map { it.id }.toSet(),
) : FavoritesRepository {
    private val state = MutableStateFlow(emptySet<String>())
    override val favoriteIds: Flow<Set<String>> = state.asStateFlow()

    override suspend fun setFavorite(productId: String, favorite: Boolean) {
        if (productId !in validProductIds) return
        state.update { current ->
            if (favorite) current + productId else current - productId
        }
    }

    override suspend fun toggleFavorite(productId: String) {
        if (productId !in validProductIds) return
        setFavorite(productId, productId !in state.value)
    }
}

class DataStoreFavoritesRepository(
    private val preferencesDataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>,
    private val validProductIds: Set<String> = WidgetCatalog.all.map { it.id }.toSet(),
) : FavoritesRepository {
    private val favoriteIdsKey = androidx.datastore.preferences.core.stringSetPreferencesKey("favorite_product_ids")

    override val favoriteIds: Flow<Set<String>> = preferencesDataStore.data
        .map { preferences ->
            preferences[favoriteIdsKey].orEmpty().filter { it in validProductIds }.toSet()
        }
        .catch { emit(emptySet<String>()) }

    override suspend fun setFavorite(productId: String, favorite: Boolean) {
        if (productId !in validProductIds) return
        preferencesDataStore.edit { preferences ->
            val current = preferences[favoriteIdsKey].orEmpty().toMutableSet()
            if (favorite) current += productId else current -= productId
            preferences[favoriteIdsKey] = current
        }
    }

    override suspend fun toggleFavorite(productId: String) {
        if (productId !in validProductIds) return
        val current = favoriteIds.first()
        setFavorite(productId, productId !in current)
    }
}
