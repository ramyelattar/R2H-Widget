package com.r2h_widget.ui.widgets

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.r2h_widget.widget.clock.ClockFontFamily
import com.r2h_widget.widget.clock.ClockSystemFontResolver

/**
 * Shared system-font mapping used by Studio preview and font samples.
 *
 * This intentionally mirrors the classic RemoteViews layouts so the
 * in-app preview and launcher widget resolve the same Android font family.
 */
fun ClockFontFamily.toComposeFontFamily(): FontFamily {
    return FontFamily(
        Typeface.create(ClockSystemFontResolver.familyName(this), Typeface.NORMAL),
    )
}
