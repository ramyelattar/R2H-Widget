package com.r2h_widget.widget.common

/**
 * Surface, effect, action, and size vocabulary shared by every widget product.
 *
 * The catalog exposes one highly customizable product per section; these types are the
 * common controls that keep previews and launcher rendering aligned across all of them.
 */

enum class WidgetBackgroundFill {
    TRANSPARENT,
    SOLID,
    LINEAR_GRADIENT,
    RADIAL_GRADIENT,
}

enum class WidgetBackgroundShape {
    ROUNDED,
    SOFT_RECTANGLE,
    PILL,
    SQUARE,
}

enum class WidgetActionType {
    NONE,
    OPEN_APP,
    OPEN_ALARM,
    OPEN_CALENDAR,
    OPEN_INSTALLED_APP,
}

enum class WidgetSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

data class WidgetBackgroundConfiguration(
    val fill: WidgetBackgroundFill = WidgetBackgroundFill.LINEAR_GRADIENT,
    val solidColorHex: String = "#12101F",
    val solidOpacity: Float = 1f,
    val gradientStartHex: String = "#171229",
    val gradientEndHex: String = "#090812",
    val gradientAngleDegrees: Float = 135f,
    val opacity: Float = 1f,
    val shape: WidgetBackgroundShape = WidgetBackgroundShape.ROUNDED,
    val cornerRadiusDp: Float = 24f,
    val borderEnabled: Boolean = true,
    val borderWidthDp: Float = 1f,
    val borderColorHex: String = "#FFFFFF",
    val borderOpacity: Float = 0.12f,
    val highlightEnabled: Boolean = true,
    val highlightColorHex: String = "#A78BFA",
    val highlightOpacity: Float = 0.16f,
)

data class WidgetEffectsConfiguration(
    val glowEnabled: Boolean = false,
    val glowColorHex: String = "#A78BFA",
    val glowStrength: Float = 0.2f,
)

data class WidgetActionConfiguration(
    val type: WidgetActionType = WidgetActionType.NONE,
    val packageName: String? = null,
)
