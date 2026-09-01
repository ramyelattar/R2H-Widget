package com.r2h_widget.widget.search

import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration

/** Where a tap on the search bar sends the user. */
enum class SearchTarget(val label: String) {
    /** Opens the device's default web search experience. */
    WEB("Web search"),

    /** Opens the system voice assistant / search assistant. */
    ASSISTANT("Assistant"),

    /** Opens a specific app chosen by package name. */
    CUSTOM_APP("App"),
}

/** What a tap on the bar itself does. */
enum class SearchTapAction {
    /** Run the configured search target immediately. */
    RUN_SEARCH,

    /** Open this widget's studio in the app. */
    OPEN_EDITOR,
}

/**
 * Typed configuration for the single Search product: a home-screen search bar that
 * launches the chosen search target in one tap.
 */
data class SearchWidgetConfiguration(
    val target: SearchTarget = SearchTarget.WEB,
    val customAppPackage: String? = null,
    val hintText: String = "Search",
    val showSearchIcon: Boolean = true,
    val showVoiceIcon: Boolean = true,
    val hintColorHex: String = "#A7A4B5",
    val iconColorHex: String = "#B69CFF",
    val textScale: Float = 1f,
    val contentPaddingDp: Float = 18f,
    val alignStart: Boolean = true,
    val background: WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(
        fill = com.r2h_widget.widget.common.WidgetBackgroundFill.SOLID,
        solidColorHex = "#15151F",
        shape = com.r2h_widget.widget.common.WidgetBackgroundShape.PILL,
        cornerRadiusDp = 100f,
        borderEnabled = true,
        borderColorHex = "#FFFFFF",
        borderOpacity = 0.14f,
        highlightEnabled = false,
    ),
    val tapAction: SearchTapAction = SearchTapAction.RUN_SEARCH,
    val action: WidgetActionConfiguration = WidgetActionConfiguration(),
)
