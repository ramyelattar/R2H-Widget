package com.r2h_widget.widget.clock

/** Canonical launcher-safe family names shared by classic layouts and Studio preview. */
object ClockSystemFontResolver {
    fun familyName(family: ClockFontFamily): String = when (family) {
        ClockFontFamily.SYSTEM -> "sans-serif"
        ClockFontFamily.ROUNDED -> "casual"
        ClockFontFamily.CONDENSED -> "sans-serif-condensed"
        ClockFontFamily.MONO -> "monospace"
        ClockFontFamily.ELEGANT -> "cursive"
        ClockFontFamily.DIGITAL -> "serif-monospace"
    }
}
