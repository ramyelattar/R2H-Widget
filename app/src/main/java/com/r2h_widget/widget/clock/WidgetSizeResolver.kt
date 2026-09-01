package com.r2h_widget.widget.clock

import com.r2h_widget.widget.common.WidgetSizeClass

object WidgetSizeResolver {
    fun fromDp(widthDp: Int, heightDp: Int): WidgetSizeClass = when {
        widthDp < 180 || heightDp < 80 -> WidgetSizeClass.COMPACT
        widthDp >= 280 || heightDp >= 140 -> WidgetSizeClass.EXPANDED
        else -> WidgetSizeClass.MEDIUM
    }

    fun scaleFor(sizeClass: WidgetSizeClass, configuration: DigitalClockConfiguration): Float = when (sizeClass) {
        WidgetSizeClass.COMPACT -> configuration.responsive.compactScale
        WidgetSizeClass.MEDIUM -> configuration.responsive.mediumScale
        WidgetSizeClass.EXPANDED -> configuration.responsive.expandedScale
    }
}
