package com.r2h_widget.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetPreviewRendererRegistryTest {
    @Test
    fun knownDigitalClockRendererResolves() {
        assertEquals(
            WidgetPreviewRendererRegistry.DIGITAL_CLOCK,
            WidgetPreviewRendererRegistry.resolve(WidgetPreviewRendererRegistry.DIGITAL_CLOCK),
        )
    }

    @Test
    fun unknownRendererUsesControlledFallback() {
        assertEquals(
            WidgetPreviewRendererRegistry.FALLBACK,
            WidgetPreviewRendererRegistry.resolve("renderer-that-does-not-exist"),
        )
    }

    @Test
    fun categoryRenderers_areRegistered() {
        assertEquals(WidgetPreviewRendererRegistry.ANALOG_CLOCK, WidgetPreviewRendererRegistry.resolve(WidgetPreviewRendererRegistry.ANALOG_CLOCK))
        assertEquals(WidgetPreviewRendererRegistry.BATTERY, WidgetPreviewRendererRegistry.resolve(WidgetPreviewRendererRegistry.BATTERY))
        assertEquals(WidgetPreviewRendererRegistry.WEATHER, WidgetPreviewRendererRegistry.resolve(WidgetPreviewRendererRegistry.WEATHER))
        assertEquals(WidgetPreviewRendererRegistry.SEARCH, WidgetPreviewRendererRegistry.resolve(WidgetPreviewRendererRegistry.SEARCH))
    }
}
