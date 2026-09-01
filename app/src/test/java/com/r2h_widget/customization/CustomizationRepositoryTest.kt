package com.r2h_widget.customization

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizationRepositoryTest {
    @Test
    fun favorites_areSetLikeAndUnknownIdsAreIgnored() = runBlocking {
        val repository = InMemoryCustomizationRepository()

        repository.toggleIconPackFavorite(IconPackCatalog.LIQUID_SIRI_GLASS_ID)
        repository.toggleIconPackFavorite(IconPackCatalog.LIQUID_SIRI_GLASS_ID)
        repository.toggleIconPackFavorite("missing")
        repository.toggleWallpaperFavorite(WallpaperCatalog.LIQUID_SIRI_GLASS_ID)

        val state = repository.selections.first()
        assertTrue(state.favoriteIconPackIds.isEmpty())
        assertEquals(setOf(WallpaperCatalog.LIQUID_SIRI_GLASS_ID), state.favoriteWallpaperIds)
    }

    @Test
    fun applyingTheme_coordinatesIconWallpaperClockAndAccent() = runBlocking {
        val repository = InMemoryCustomizationRepository()

        repository.applyTheme(ThemeCatalog.LUXURY_VIP_ID)

        assertEquals(
            CustomizationSelections(
                iconPackId = IconPackCatalog.LUXURY_VIP_ID,
                wallpaperId = WallpaperCatalog.LUXURY_VIP_ID,
                themeId = ThemeCatalog.LUXURY_VIP_ID,
                clockPresetId = "minimal-amoled",
                accentHex = "#D4AF37",
            ),
            repository.selections.first(),
        )
    }

    @Test
    fun selectingUnknownThemeDoesNotChangeCurrentBundle() = runBlocking {
        val repository = InMemoryCustomizationRepository()
        val before = repository.selections.first()

        repository.applyTheme("missing")

        assertEquals(before, repository.selections.first())
    }

    @Test
    fun dataStoreRepository_persistsThemeSelection() = runBlocking {
        val directory = Files.createTempDirectory("customization").toFile()
        val file = File(directory, "customization.preferences_pb")
        try {
            val repository = DataStoreCustomizationRepository(
                androidx.datastore.preferences.core.PreferenceDataStoreFactory.create { file },
            )

            repository.applyTheme(ThemeCatalog.LIQUID_DARK_GLASS_ID)

            val selections = repository.selections.first()
            assertEquals(IconPackCatalog.LIQUID_DARK_GLASS_ID, selections.iconPackId)
            assertEquals(WallpaperCatalog.LIQUID_DARK_GLASS_ID, selections.wallpaperId)
            assertEquals(ThemeCatalog.LIQUID_DARK_GLASS_ID, selections.themeId)
            assertEquals("midnight-glass", selections.clockPresetId)
            assertEquals("#8B7CFF", selections.accentHex)
        } finally {
            directory.deleteRecursively()
        }
    }
}
