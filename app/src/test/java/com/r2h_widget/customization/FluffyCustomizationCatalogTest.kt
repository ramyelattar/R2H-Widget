package com.r2h_widget.customization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FluffyCustomizationCatalogTest {
    @Test
    fun fluffyCollectionIsRegisteredAsOnePrimaryPack() {
        assertEquals(4, IconPackCatalog.builtIn.size)
        assertNotNull(IconPackCatalog.findById(IconPackCatalog.FLUFFY_ID))
    }

    @Test
    fun fluffyWallpaperAndThemeResolveToTheSameCollection() {
        assertEquals(4, WallpaperCatalog.builtIn.size)
        assertEquals(4, ThemeCatalog.builtIn.size)
        assertNotNull(WallpaperCatalog.findById(WallpaperCatalog.FLUFFY_ID))
        assertNotNull(ThemeCatalog.findById(ThemeCatalog.FLUFFY_ID))
    }

    @Test
    fun catalogIdsRemainUnique() {
        assertEquals(IconPackCatalog.builtIn.size, IconPackCatalog.builtIn.map { it.id }.toSet().size)
        assertEquals(WallpaperCatalog.builtIn.size, WallpaperCatalog.builtIn.map { it.id }.toSet().size)
        assertEquals(ThemeCatalog.builtIn.size, ThemeCatalog.builtIn.map { it.id }.toSet().size)
    }
}
