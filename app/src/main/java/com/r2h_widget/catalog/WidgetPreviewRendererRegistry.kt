package com.r2h_widget.catalog

object WidgetPreviewRendererRegistry {
    const val DIGITAL_CLOCK = "digital_clock"
    const val ANALOG_CLOCK = "analog_clock"
    const val BATTERY = "battery"
    const val WEATHER = "weather"
    const val SEARCH = "search"
    const val MUSIC = "music"
    const val FALLBACK = "fallback"

    fun resolve(rendererKey: String): String = when (rendererKey) {
        DIGITAL_CLOCK -> DIGITAL_CLOCK
        ANALOG_CLOCK -> ANALOG_CLOCK
        BATTERY -> BATTERY
        WEATHER -> WEATHER
        SEARCH -> SEARCH
        MUSIC -> MUSIC
        else -> FALLBACK
    }
}


