package com.r2h_widget.customization

import com.r2h_widget.widget.clock.ClockPresetCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizationCatalogTest {
    @Test
    fun primaryIconPacks_areExactlyTheFourProductionCollections() {
        assertEquals(
            listOf("fluffy", "liquid-dark-glass", "liquid-siri-glass", "luxury-vip"),
            IconPackCatalog.builtIn.map { it.id },
        )
        assertEquals(4, IconPackCatalog.builtIn.map { it.id }.toSet().size)
        assertTrue(IconPackCatalog.builtIn.all { it.previewDrawableRes != 0 })
        assertEquals(4, IconPackCatalog.builtIn.map { it.style }.toSet().size)
    }

    @Test
    fun suppliedWallpapers_areExactlyTheFourProductionCollections() {
        assertEquals(
            listOf(
                WallpaperCatalog.FLUFFY_ID,
                WallpaperCatalog.LIQUID_DARK_GLASS_ID,
                WallpaperCatalog.LIQUID_SIRI_GLASS_ID,
                WallpaperCatalog.LUXURY_VIP_ID,
            ),
            WallpaperCatalog.builtIn.map { it.id },
        )
        assertTrue(WallpaperCatalog.builtIn.all { it.drawableRes != 0 })
        assertTrue(WallpaperCatalog.builtIn.any { it.category == WallpaperCategory.DARK })
        assertTrue(WallpaperCatalog.builtIn.any { it.category == WallpaperCategory.COLOR })
        assertEquals(WallpaperCatalog.builtIn.size, WallpaperCatalog.builtIn.map { it.id }.toSet().size)
    }

    @Test
    fun themes_areExactlyFourAndReferenceExistingSelections() {
        assertEquals(
            listOf(
                ThemeCatalog.FLUFFY_ID,
                ThemeCatalog.LIQUID_DARK_GLASS_ID,
                ThemeCatalog.LIQUID_SIRI_GLASS_ID,
                ThemeCatalog.LUXURY_VIP_ID,
            ),
            ThemeCatalog.builtIn.map { it.id },
        )
        ThemeCatalog.builtIn.forEach { theme ->
            assertTrue(IconPackCatalog.findById(theme.iconPackId) != null)
            assertTrue(WallpaperCatalog.findById(theme.wallpaperId) != null)
            assertTrue(ClockPresetCatalog.findById(theme.clockPresetId) != null)
            assertTrue(theme.previewDrawableRes != 0)
        }
    }
}
