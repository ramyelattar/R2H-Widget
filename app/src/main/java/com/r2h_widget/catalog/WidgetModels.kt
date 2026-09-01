package com.r2h_widget.catalog

enum class WidgetCategory(val label: String) {
    ALL("All"),
    CLOCK("Clock"),
    BATTERY("Battery"),
    WEATHER("Weather"),
    SEARCH("Search"),
    MUSIC("Music"),
}

enum class WidgetSize(val label: String) {
    TWO_BY_ONE("2 x 1"),
    TWO_BY_TWO("2 x 2"),
    FOUR_BY_TWO("4 x 2"),
}

enum class PremiumState {
    FREE,
    PRO,
}

/** Which real launcher provider backs a catalog product. */
enum class HomeWidgetAvailability {
    NONE,
    DIGITAL_CLOCK,
    ANALOG_CLOCK,
    BATTERY,
    WEATHER,
    SEARCH,
    MUSIC,
}

data class WidgetDefinition(
    val id: String,
    val displayName: String,
    val description: String,
    val category: WidgetCategory,
    val size: WidgetSize,
    val premiumState: PremiumState = PremiumState.FREE,
    val rendererKey: String,
    val previewAccentHex: String = "#8B5CF6",
    val homeWidgetAvailability: HomeWidgetAvailability = HomeWidgetAvailability.NONE,
)


