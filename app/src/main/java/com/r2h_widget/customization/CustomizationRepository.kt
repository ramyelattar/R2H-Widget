package com.r2h_widget.customization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface CustomizationRepository {
    val selections: Flow<CustomizationSelections>

    suspend fun selectIconPack(id: String)
    suspend fun toggleIconPackFavorite(id: String)
    suspend fun selectWallpaper(id: String)
    suspend fun toggleWallpaperFavorite(id: String)
    suspend fun applyTheme(id: String)
}

class InMemoryCustomizationRepository : CustomizationRepository {
    private val state = MutableStateFlow(CustomizationSelections())
    override val selections: Flow<CustomizationSelections> = state.asStateFlow()

    override suspend fun selectIconPack(id: String) {
        if (IconPackCatalog.findById(id) != null) state.update { it.copy(iconPackId = id) }
    }

    override suspend fun toggleIconPackFavorite(id: String) {
        if (IconPackCatalog.findById(id) == null) return
        state.update { current ->
            val favorites = current.favoriteIconPackIds.toMutableSet()
            if (!favorites.add(id)) favorites.remove(id)
            current.copy(favoriteIconPackIds = favorites)
        }
    }

    override suspend fun selectWallpaper(id: String) {
        if (WallpaperCatalog.findById(id) != null) state.update { it.copy(wallpaperId = id) }
    }

    override suspend fun toggleWallpaperFavorite(id: String) {
        if (WallpaperCatalog.findById(id) == null) return
        state.update { current ->
            val favorites = current.favoriteWallpaperIds.toMutableSet()
            if (!favorites.add(id)) favorites.remove(id)
            current.copy(favoriteWallpaperIds = favorites)
        }
    }

    override suspend fun applyTheme(id: String) {
        val theme = ThemeCatalog.findById(id) ?: return
        state.value = state.value.copy(
            themeId = theme.id,
            iconPackId = theme.iconPackId,
            wallpaperId = theme.wallpaperId,
            clockPresetId = theme.clockPresetId,
            accentHex = theme.accentHex,
        )
    }
}

class DataStoreCustomizationRepository(
    private val dataStore: DataStore<Preferences>,
) : CustomizationRepository {
    override val selections: Flow<CustomizationSelections> = dataStore.data
        .map { preferences ->
            val iconPackId = preferences[Keys.iconPackId]
                ?.takeIf { IconPackCatalog.findById(it) != null }
                ?: CustomizationSelections().iconPackId
            val wallpaperId = preferences[Keys.wallpaperId]
                ?.takeIf { WallpaperCatalog.findById(it) != null }
                ?: CustomizationSelections().wallpaperId
            val theme = preferences[Keys.themeId]
                ?.let(ThemeCatalog::findById)
                ?: ThemeCatalog.findById(CustomizationSelections().themeId)!!
            CustomizationSelections(
                iconPackId = iconPackId,
                favoriteIconPackIds = preferences[Keys.favoriteIconPackIds]
                    .orEmpty()
                    .filter { IconPackCatalog.findById(it) != null }
                    .toSet(),
                wallpaperId = wallpaperId,
                favoriteWallpaperIds = preferences[Keys.favoriteWallpaperIds]
                    .orEmpty()
                    .filter { WallpaperCatalog.findById(it) != null }
                    .toSet(),
                themeId = theme.id,
                clockPresetId = preferences[Keys.clockPresetId] ?: theme.clockPresetId,
                accentHex = preferences[Keys.accentHex] ?: theme.accentHex,
            )
        }
        .catch { emit(CustomizationSelections()) }

    override suspend fun selectIconPack(id: String) {
        if (IconPackCatalog.findById(id) == null) return
        dataStore.edit { it[Keys.iconPackId] = id }
    }

    override suspend fun toggleIconPackFavorite(id: String) {
        if (IconPackCatalog.findById(id) == null) return
        dataStore.edit { preferences ->
            val current = preferences[Keys.favoriteIconPackIds].orEmpty().toMutableSet()
            if (!current.add(id)) current.remove(id)
            preferences[Keys.favoriteIconPackIds] = current
        }
    }

    override suspend fun selectWallpaper(id: String) {
        if (WallpaperCatalog.findById(id) == null) return
        dataStore.edit { it[Keys.wallpaperId] = id }
    }

    override suspend fun toggleWallpaperFavorite(id: String) {
        if (WallpaperCatalog.findById(id) == null) return
        dataStore.edit { preferences ->
            val current = preferences[Keys.favoriteWallpaperIds].orEmpty().toMutableSet()
            if (!current.add(id)) current.remove(id)
            preferences[Keys.favoriteWallpaperIds] = current
        }
    }

    override suspend fun applyTheme(id: String) {
        val theme = ThemeCatalog.findById(id) ?: return
        dataStore.edit { preferences ->
            preferences[Keys.themeId] = theme.id
            preferences[Keys.iconPackId] = theme.iconPackId
            preferences[Keys.wallpaperId] = theme.wallpaperId
            preferences[Keys.clockPresetId] = theme.clockPresetId
            preferences[Keys.accentHex] = theme.accentHex
        }
    }

    suspend fun current(): CustomizationSelections = selections.first()

    private object Keys {
        val iconPackId = stringPreferencesKey("customization_icon_pack_id")
        val favoriteIconPackIds = stringSetPreferencesKey("customization_favorite_icon_pack_ids")
        val wallpaperId = stringPreferencesKey("customization_wallpaper_id")
        val favoriteWallpaperIds = stringSetPreferencesKey("customization_favorite_wallpaper_ids")
        val themeId = stringPreferencesKey("customization_theme_id")
        val clockPresetId = stringPreferencesKey("customization_clock_preset_id")
        val accentHex = stringPreferencesKey("customization_accent_hex")
    }
}
