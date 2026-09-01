package com.r2h_widget.customization

import com.r2h_widget.R

object IconPackCatalog {
    const val FLUFFY_ID = "fluffy"
    const val LIQUID_DARK_GLASS_ID = "liquid-dark-glass"
    const val LIQUID_SIRI_GLASS_ID = "liquid-siri-glass"
    const val LUXURY_VIP_ID = "luxury-vip"

    val builtIn: List<IconPackDefinition> = listOf(
        IconPackDefinition(
            id = FLUFFY_ID,
            name = "Fluffy",
            description = "Plush rounded tiles with tactile fibers and soft premium depth.",
            style = IconPackStyle.FLUFFY,
            previewDrawableRes = R.drawable.customization_fluffy_preview,
            accentHex = "#FF7FA8",
        ),
        IconPackDefinition(
            id = LIQUID_DARK_GLASS_ID,
            name = "Liquid Dark Glass",
            description = "Smoked AMOLED glass with glossy depth and restrained violet light.",
            style = IconPackStyle.LIQUID_DARK_GLASS,
            previewDrawableRes = R.drawable.customization_liquid_dark_glass_preview,
            accentHex = "#8B7CFF",
        ),
        IconPackDefinition(
            id = LIQUID_SIRI_GLASS_ID,
            name = "Liquid Siri Glass",
            description = "Luminous frosted glass with pearl, aqua, lilac, and soft pink refraction.",
            style = IconPackStyle.LIQUID_SIRI_GLASS,
            previewDrawableRes = R.drawable.customization_liquid_siri_glass_preview,
            accentHex = "#74C7FF",
        ),
        IconPackDefinition(
            id = LUXURY_VIP_ID,
            name = "Luxury VIP",
            description = "Black onyx surfaces, metallic champagne edges, and embossed detail.",
            style = IconPackStyle.LUXURY_VIP,
            previewDrawableRes = R.drawable.customization_luxury_vip_preview,
            accentHex = "#D4AF37",
        ),
    )

    fun findById(id: String): IconPackDefinition? = builtIn.firstOrNull { it.id == id }
}

object WallpaperCatalog {
    const val FLUFFY_ID = "fluffy-wallpaper"
    const val LIQUID_DARK_GLASS_ID = "liquid-dark-glass-wallpaper"
    const val LIQUID_SIRI_GLASS_ID = "liquid-siri-glass-wallpaper"
    const val LUXURY_VIP_ID = "luxury-vip-wallpaper"

    val builtIn: List<WallpaperDefinition> = listOf(
        WallpaperDefinition(
            id = FLUFFY_ID,
            name = "Fluffy Wallpaper",
            description = "A soft textile backdrop designed to complement plush icons.",
            category = WallpaperCategory.SOFT,
            drawableRes = R.drawable.customization_wallpaper_fluffy,
            accentHex = "#FF7FA8",
        ),
        WallpaperDefinition(
            id = LIQUID_DARK_GLASS_ID,
            name = "Liquid Dark Glass Wallpaper",
            description = "AMOLED black with blue-violet and magenta glass light.",
            category = WallpaperCategory.DARK,
            drawableRes = R.drawable.customization_wallpaper_liquid_dark_glass,
            accentHex = "#8B7CFF",
        ),
        WallpaperDefinition(
            id = LIQUID_SIRI_GLASS_ID,
            name = "Liquid Siri Glass Wallpaper",
            description = "Pearl, aqua, sky blue, lilac, and soft pink luminous glass.",
            category = WallpaperCategory.COLOR,
            drawableRes = R.drawable.customization_wallpaper_liquid_siri_glass,
            accentHex = "#74C7FF",
        ),
        WallpaperDefinition(
            id = LUXURY_VIP_ID,
            name = "Luxury VIP Wallpaper",
            description = "Graphite-black onyx with restrained champagne-gold detail.",
            category = WallpaperCategory.DARK,
            drawableRes = R.drawable.customization_wallpaper_luxury_vip,
            accentHex = "#D4AF37",
        ),
    )

    fun findById(id: String): WallpaperDefinition? = builtIn.firstOrNull { it.id == id }
    fun forCategory(category: WallpaperCategory): List<WallpaperDefinition> = when (category) {
        WallpaperCategory.ALL -> builtIn
        else -> builtIn.filter { it.category == category }
    }
}

object ThemeCatalog {
    const val FLUFFY_ID = "fluffy-theme"
    const val LIQUID_DARK_GLASS_ID = "liquid-dark-glass-theme"
    const val LIQUID_SIRI_GLASS_ID = "liquid-siri-glass-theme"
    const val LUXURY_VIP_ID = "luxury-vip-theme"

    val builtIn: List<ThemeDefinition> = listOf(
        ThemeDefinition(
            id = FLUFFY_ID,
            name = "Fluffy Theme",
            description = "Plush icons, soft texture, and a warm pink accent.",
            iconPackId = IconPackCatalog.FLUFFY_ID,
            wallpaperId = WallpaperCatalog.FLUFFY_ID,
            clockPresetId = "midnight-glass",
            accentHex = "#FF7FA8",
            previewDrawableRes = R.drawable.customization_wallpaper_fluffy,
        ),
        ThemeDefinition(
            id = LIQUID_DARK_GLASS_ID,
            name = "Liquid Dark Glass Theme",
            description = "Smoked glass icons, AMOLED darkness, and violet light.",
            iconPackId = IconPackCatalog.LIQUID_DARK_GLASS_ID,
            wallpaperId = WallpaperCatalog.LIQUID_DARK_GLASS_ID,
            clockPresetId = "midnight-glass",
            accentHex = "#8B7CFF",
            previewDrawableRes = R.drawable.customization_wallpaper_liquid_dark_glass,
        ),
        ThemeDefinition(
            id = LIQUID_SIRI_GLASS_ID,
            name = "Liquid Siri Glass Theme",
            description = "Luminous frosted icons with an airy blue accent.",
            iconPackId = IconPackCatalog.LIQUID_SIRI_GLASS_ID,
            wallpaperId = WallpaperCatalog.LIQUID_SIRI_GLASS_ID,
            clockPresetId = "neon-pulse",
            accentHex = "#74C7FF",
            previewDrawableRes = R.drawable.customization_wallpaper_liquid_siri_glass,
        ),
        ThemeDefinition(
            id = LUXURY_VIP_ID,
            name = "Luxury VIP Theme",
            description = "Onyx surfaces, champagne-metal details, and a gold accent.",
            iconPackId = IconPackCatalog.LUXURY_VIP_ID,
            wallpaperId = WallpaperCatalog.LUXURY_VIP_ID,
            clockPresetId = "minimal-amoled",
            accentHex = "#D4AF37",
            previewDrawableRes = R.drawable.customization_wallpaper_luxury_vip,
        ),
    )

    fun findById(id: String): ThemeDefinition? = builtIn.firstOrNull { it.id == id }
}
