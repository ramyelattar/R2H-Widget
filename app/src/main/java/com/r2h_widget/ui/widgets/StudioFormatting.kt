package com.r2h_widget.ui.widgets

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

fun formatStudioNumber(value: Float, signed: Boolean = false): String {
    val rounded = value.roundToInt()
    if (rounded == 0) return "0"
    return if (signed && rounded > 0) "+$rounded" else rounded.toString()
}

fun formatStudioPercent(value: Float): String = "${(value.coerceIn(0f, 1f) * 100f).roundToInt()}%"

fun formatStudioScale(value: Float): String = "${(value * 100f).roundToInt()}%"

fun formatStudioDecimal(value: Float): String = String.format(Locale.US, "%.1f", value)

fun isStudioNearlyZero(value: Float): Boolean = abs(value) < 0.5f
