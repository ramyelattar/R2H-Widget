package com.r2h_widget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.r2h_widget.widget.clock.ClockWidgetPreset
import com.r2h_widget.widget.clock.DigitalClockConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface WidgetPresetRepository {
    val customPresets: Flow<List<ClockWidgetPreset>>

    suspend fun save(preset: ClockWidgetPreset)

    suspend fun delete(presetId: String)
}

class InMemoryWidgetPresetRepository : WidgetPresetRepository {
    private val presets = kotlinx.coroutines.flow.MutableStateFlow<List<ClockWidgetPreset>>(emptyList())
    override val customPresets: Flow<List<ClockWidgetPreset>> = presets

    override suspend fun save(preset: ClockWidgetPreset) {
        presets.value = (presets.value.filterNot { it.id == preset.id } + preset)
    }

    override suspend fun delete(presetId: String) {
        presets.value = presets.value.filterNot { it.id == presetId }
    }
}

class DataStoreWidgetPresetRepository(
    private val preferencesDataStore: DataStore<Preferences>,
) : WidgetPresetRepository {
    override val customPresets: Flow<List<ClockWidgetPreset>> = preferencesDataStore.data.map { preferences ->
        preferences[presetIdsKey].orEmpty().mapNotNull { id ->
            val stored = preferences[presetKey(id)] ?: return@mapNotNull null
            val fields = stored.split("|", limit = 2)
            if (fields.size != 2) return@mapNotNull null
            val instance = WidgetInstanceCodec.decode(0, fields[1]) ?: return@mapNotNull null
            ClockWidgetPreset(id = id, name = fields[0], configuration = instance.configuration)
        }.sortedBy { it.name }
    }

    override suspend fun save(preset: ClockWidgetPreset) {
        preferencesDataStore.edit { preferences ->
            preferences[presetIdsKey] = preferences[presetIdsKey].orEmpty() + preset.id
            preferences[presetKey(preset.id)] = "${preset.name.replace("|", " ")}|${WidgetInstanceCodec.encode(
                WidgetInstance(0, com.r2h_widget.catalog.WidgetProductIds.DIGITAL_CLOCK, preset.configuration),
            )}"
        }
    }

    override suspend fun delete(presetId: String) {
        preferencesDataStore.edit { preferences ->
            preferences[presetIdsKey] = preferences[presetIdsKey].orEmpty() - presetId
            preferences.remove(presetKey(presetId))
        }
    }

    private fun presetKey(id: String): Preferences.Key<String> = stringPreferencesKey("custom_preset_$id")

    private companion object {
        val presetIdsKey = stringSetPreferencesKey("custom_preset_ids")
    }
}
