package com.r2h_widget.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetCatalogTest {
    @Test
    fun catalog_containsTheAuthoritativeBaseWidgets() {
        assertEquals(
            listOf("digital-clock", "analog-clock", "battery", "weather", "search", "music"),
            WidgetCatalog.all.map(WidgetDefinition::id),
        )
    }

    @Test
    fun catalog_usesFinalSimpleUserFacingNames() {
        assertEquals(
            listOf("Digital Clock", "Analog Clock", "Battery", "Weather", "Search", "Music Player"),
            WidgetCatalog.all.map(WidgetDefinition::displayName),
        )
    }

    @Test
    fun catalog_usesUniqueStableIds() {
        val ids = WidgetCatalog.all.map(WidgetDefinition::id)

        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { it.isNotBlank() })
    }

    @Test
    fun catalog_coversTheAuthoritativeCategories() {
        assertEquals(
            setOf(WidgetCategory.CLOCK, WidgetCategory.BATTERY, WidgetCategory.WEATHER, WidgetCategory.SEARCH, WidgetCategory.MUSIC),
            WidgetCatalog.all.map(WidgetDefinition::category).toSet(),
        )
        assertEquals(
            listOf(WidgetCategory.ALL, WidgetCategory.CLOCK, WidgetCategory.BATTERY, WidgetCategory.WEATHER, WidgetCategory.SEARCH, WidgetCategory.MUSIC),
            WidgetCatalog.categories,
        )
    }

    @Test
    fun clockCategory_holdsBothClocksWithoutPresetDuplicates() {
        val clocks = WidgetCatalog.forCategory(WidgetCategory.CLOCK)

        assertEquals(listOf("digital-clock", "analog-clock"), clocks.map(WidgetDefinition::id))
    }

    @Test
    fun everyProduct_isAvailableToItsRealHomeWidgetProvider() {
        WidgetCatalog.all.forEach { definition ->
            assertTrue(
                "${definition.id} must be pinnable",
                definition.homeWidgetAvailability != HomeWidgetAvailability.NONE,
            )
        }
    }

    @Test
    fun everyProduct_usesItsCategoryRenderer() {
        WidgetCatalog.all.forEach { definition ->
            assertEquals(
                "${definition.id} must keep its renderer key",
                definition.rendererKey,
                WidgetPreviewRendererRegistry.resolve(definition.rendererKey),
            )
        }
    }

    @Test
    fun unknownRendererKey_resolvesToFallback() {
        assertEquals(
            WidgetPreviewRendererRegistry.FALLBACK,
            WidgetPreviewRendererRegistry.resolve("not_registered"),
        )
    }

    @Test
    fun lookup_resolvesValidIdAndReturnsNullForUnknownId() {
        assertEquals("Digital Clock", WidgetCatalog.findById("digital-clock")?.displayName)
        assertNull(WidgetCatalog.findById("missing"))
    }

    @Test
    fun legacyPresetIds_foldIntoTheirCanonicalProducts() {
        assertEquals(WidgetCatalog.digitalClock, WidgetCatalog.findById("clock-01"))
        assertEquals(WidgetCatalog.digitalClock, WidgetCatalog.findById("clock-10"))
        assertEquals(WidgetCatalog.battery, WidgetCatalog.findById("battery-foundation"))
        assertEquals(WidgetCatalog.weather, WidgetCatalog.findById("weather-foundation"))
    }
}
