package com.r2h_widget.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WidgetProductionCoverageTest {
    @Test
    fun everyCatalogEntry_isAvailableToARealHomeWidgetProvider() {
        WidgetCatalog.all.forEach { definition ->
            assertNotEquals(
                "${definition.id} has no launcher provider",
                HomeWidgetAvailability.NONE,
                definition.homeWidgetAvailability,
            )
        }
    }

    @Test
    fun everyCatalogEntry_usesACategoryRendererInsteadOfFallback() {
        WidgetCatalog.all.forEach { definition ->
            assertNotEquals(
                "${definition.id} is still wired to the placeholder renderer",
                WidgetPreviewRendererRegistry.FALLBACK,
                definition.rendererKey,
            )
        }
    }

    @Test
    fun productIds_canonicalizeEveryLegacyPreset() {
        (1..10).forEach { index ->
            assertEquals(
                WidgetProductIds.DIGITAL_CLOCK,
                WidgetProductIds.canonicalize("clock-%02d".format(index)),
            )
        }
        assertEquals(WidgetProductIds.BATTERY, WidgetProductIds.canonicalize("battery-foundation"))
        assertEquals(WidgetProductIds.WEATHER, WidgetProductIds.canonicalize("weather-foundation"))
        assertEquals(WidgetProductIds.DIGITAL_CLOCK, WidgetProductIds.canonicalize("digital-clock"))
        assertEquals("calendar-foundation", WidgetProductIds.canonicalize("calendar-foundation"))
    }
}
