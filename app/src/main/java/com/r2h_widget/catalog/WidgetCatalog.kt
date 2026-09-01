package com.r2h_widget.catalog

/**
 * The user-facing catalog: exactly one base widget per section.
 *
 * Digital and analog clocks share the Clock category; everything a user could want
 * from the removed preset products is a studio control on these six definitions.
 */
object WidgetCatalog {
    val categories: List<WidgetCategory> = listOf(
        WidgetCategory.ALL,
        WidgetCategory.CLOCK,
        WidgetCategory.BATTERY,
        WidgetCategory.WEATHER,
        WidgetCategory.SEARCH,
        WidgetCategory.MUSIC,
    )

    val digitalClock = WidgetDefinition(
        id = WidgetProductIds.DIGITAL_CLOCK,
        displayName = "Digital Clock",
        description = "Big readable time with date, fonts, and colors you control.",
        category = WidgetCategory.CLOCK,
        size = WidgetSize.TWO_BY_ONE,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.DIGITAL_CLOCK,
        previewAccentHex = "#A78BFA",
        homeWidgetAvailability = HomeWidgetAvailability.DIGITAL_CLOCK,
    )

    val analogClock = WidgetDefinition(
        id = WidgetProductIds.ANALOG_CLOCK,
        displayName = "Analog Clock",
        description = "A classic dial with markers, hands, and a cap you can restyle.",
        category = WidgetCategory.CLOCK,
        size = WidgetSize.TWO_BY_TWO,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.ANALOG_CLOCK,
        previewAccentHex = "#38BDF8",
        homeWidgetAvailability = HomeWidgetAvailability.ANALOG_CLOCK,
    )

    val battery = WidgetDefinition(
        id = WidgetProductIds.BATTERY,
        displayName = "Battery",
        description = "Live level, charging state, and colors driven by your device.",
        category = WidgetCategory.BATTERY,
        size = WidgetSize.TWO_BY_ONE,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.BATTERY,
        previewAccentHex = "#34D399",
        homeWidgetAvailability = HomeWidgetAvailability.BATTERY,
    )

    val weather = WidgetDefinition(
        id = WidgetProductIds.WEATHER,
        displayName = "Weather",
        description = "Temperature, condition, and place once a weather source is connected.",
        category = WidgetCategory.WEATHER,
        size = WidgetSize.TWO_BY_TWO,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.WEATHER,
        previewAccentHex = "#7DD3FC",
        homeWidgetAvailability = HomeWidgetAvailability.WEATHER,
    )

    val search = WidgetDefinition(
        id = WidgetProductIds.SEARCH,
        displayName = "Search",
        description = "A home-screen search bar that opens your search app in one tap.",
        category = WidgetCategory.SEARCH,
        size = WidgetSize.FOUR_BY_TWO,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.SEARCH,
        previewAccentHex = "#B69CFF",
        homeWidgetAvailability = HomeWidgetAvailability.SEARCH,
    )

    val music = WidgetDefinition(
        id = WidgetProductIds.MUSIC,
        displayName = "Music Player",
        description = "Album art, track info, progress, and playback controls linked to your chosen player.",
        category = WidgetCategory.MUSIC,
        size = WidgetSize.FOUR_BY_TWO,
        premiumState = PremiumState.FREE,
        rendererKey = WidgetPreviewRendererRegistry.MUSIC,
        previewAccentHex = "#A78BFA",
        homeWidgetAvailability = HomeWidgetAvailability.MUSIC,
    )

    val all: List<WidgetDefinition> = listOf(
        digitalClock,
        analogClock,
        battery,
        weather,
        search,
        music,
    )

    fun forCategory(category: WidgetCategory): List<WidgetDefinition> = when (category) {
        WidgetCategory.ALL -> all
        else -> all.filter { it.category == category }
    }

    fun findById(id: String): WidgetDefinition? = all.firstOrNull { it.id == WidgetProductIds.canonicalize(id) }
}



