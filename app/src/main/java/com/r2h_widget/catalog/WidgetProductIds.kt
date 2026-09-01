package com.r2h_widget.catalog

/**
 * Stable product IDs for the simplified catalog: one base widget per section.
 *
 * Anything a user can customize is a control inside the studio, never a separate
 * catalog entry. Legacy preset IDs are folded into these canonical products.
 */
object WidgetProductIds {
    const val DIGITAL_CLOCK = "digital-clock"
    const val ANALOG_CLOCK = "analog-clock"
    const val BATTERY = "battery"
    const val WEATHER = "weather"
    const val SEARCH = "search"
    const val MUSIC = "music"

    /**
     * Legacy products that may still exist as pinned home-screen widgets or stored
     * records. They are not offered in the catalog anymore.
     */
    const val LEGACY_CALENDAR = "calendar-foundation"

    /**
     * Maps every historical product ID onto the current canonical product.
     * `clock-01` .. `clock-10` were cosmetic digital clock presets; each collapses
     * into the single Digital Clock product with its configuration preserved.
     */
    fun canonicalize(productId: String): String = when {
        productId.startsWith("clock-") -> DIGITAL_CLOCK
        productId == "battery-foundation" -> BATTERY
        productId == "weather-foundation" -> WEATHER
        else -> productId
    }
}

