package com.r2h_widget.customization

import android.graphics.Bitmap

enum class IconPackStyle {
    FLUFFY,
    LIQUID_DARK_GLASS,
    LIQUID_SIRI_GLASS,
    LUXURY_VIP,
}

data class IconPackDefinition(
    val id: String,
    val name: String,
    val description: String,
    val style: IconPackStyle,
    val previewDrawableRes: Int,
    val accentHex: String,
)

data class DiscoveredAppIcon(
    val packageName: String,
    val activityName: String,
    val label: String,
    val bitmap: Bitmap,
    val mapped: Boolean,
)

enum class WallpaperCategory(val label: String) {
    ALL("All"),
    DARK("Dark"),
    COLOR("Color"),
    SOFT("Soft"),
}

data class WallpaperDefinition(
    val id: String,
    val name: String,
    val description: String,
    val category: WallpaperCategory,
    val drawableRes: Int,
    val accentHex: String,
)

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH,
}

data class ThemeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconPackId: String,
    val wallpaperId: String,
    val clockPresetId: String,
    val accentHex: String,
    val previewDrawableRes: Int,
)

data class CustomizationSelections(
    val iconPackId: String = IconPackCatalog.LIQUID_DARK_GLASS_ID,
    val favoriteIconPackIds: Set<String> = emptySet(),
    val wallpaperId: String = WallpaperCatalog.LIQUID_DARK_GLASS_ID,
    val favoriteWallpaperIds: Set<String> = emptySet(),
    val themeId: String = ThemeCatalog.LIQUID_DARK_GLASS_ID,
    val clockPresetId: String = "midnight-glass",
    val accentHex: String = "#8B7CFF",
)

sealed interface IconApplicationResult {
    data class ManualSelectionRequired(val message: String) : IconApplicationResult
    data class Unsupported(val message: String) : IconApplicationResult
}

sealed interface WallpaperApplyResult {
    data object Applied : WallpaperApplyResult
    data class Failed(val message: String) : WallpaperApplyResult
}
