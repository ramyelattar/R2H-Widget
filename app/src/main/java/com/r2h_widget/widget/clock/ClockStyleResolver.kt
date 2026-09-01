package com.r2h_widget.widget.clock

import com.r2h_widget.widget.common.WidgetSizeClass
import kotlin.math.roundToInt

/**
 * Rendering values shared by the launcher renderer and the Compose preview.
 *
 * This type deliberately contains no Android UI types. Colors are packed ARGB
 * integers so that the semantic rendering contract remains host-testable.
 */
data class ResolvedClockStyle(
    val responsiveScale: Float,
    val timeSizeSp: Float,
    val dateSizeSp: Float,
    val timeAlignment: ClockTimeAlignment,
    val dateAlignment: ClockDateAlignment,
    val timeColor: Int,
    val dateColor: Int,
    val weekdayColor: Int,
    val contentPaddingDp: Float,
    val timeDateGapDp: Float,
    val horizontalOffsetDp: Float,
    val verticalOffsetDp: Float,
    val timeFontFamily: ClockFontFamily,
    val dateFontFamily: ClockFontFamily,
    val timeFontWeight: Int,
    val dateFontWeight: Int,
    val letterSpacing: Float,
    val isDateVisible: Boolean,
    val isWeekdayVisible: Boolean,
)

object ClockStyleResolver {
    const val BASE_TIME_SIZE_SP = 38f

    fun resolve(
        configuration: DigitalClockConfiguration,
        sizeClass: WidgetSizeClass,
    ): ResolvedClockStyle {
        val responsiveScale = WidgetSizeResolver.scaleFor(sizeClass, configuration)
        val compactDateVisible = sizeClass != WidgetSizeClass.COMPACT || configuration.responsive.compactShowDate
        val dateColor = applyAlpha(
            parseColor(configuration.typography.dateColorHex, DEFAULT_DATE_COLOR),
            configuration.typography.dateOpacity,
        )
        val weekdayColor = applyAlpha(
            parseColor(configuration.typography.timeColorHex, DEFAULT_TIME_COLOR),
            configuration.typography.dateOpacity,
        )

        return ResolvedClockStyle(
            responsiveScale = responsiveScale,
            timeSizeSp = BASE_TIME_SIZE_SP * configuration.typography.timeScale * responsiveScale,
            dateSizeSp = BASE_TIME_SIZE_SP * configuration.typography.dateScale * responsiveScale,
            timeAlignment = configuration.content.alignment,
            dateAlignment = configuration.date.alignment,
            timeColor = parseColor(configuration.typography.timeColorHex, DEFAULT_TIME_COLOR),
            dateColor = dateColor,
            weekdayColor = weekdayColor,
            contentPaddingDp = configuration.layout.contentPaddingDp,
            timeDateGapDp = configuration.layout.timeDateGapDp,
            horizontalOffsetDp = configuration.layout.horizontalOffsetDp,
            verticalOffsetDp = configuration.layout.verticalOffsetDp,
            timeFontFamily = configuration.typography.timeFontFamily,
            dateFontFamily = configuration.typography.dateFontFamily,
            timeFontWeight = configuration.typography.timeFontWeight,
            dateFontWeight = configuration.typography.dateFontWeight,
            letterSpacing = configuration.typography.letterSpacing,
            isDateVisible = configuration.date.enabled && compactDateVisible,
            isWeekdayVisible = configuration.date.weekdayStyle != ClockWeekdayStyle.HIDDEN && compactDateVisible,
        )
    }

    private fun parseColor(value: String, fallback: Int): Int {
        val hex = value.trim().removePrefix("#")
        val parsed = hex.toLongOrNull(16) ?: return fallback
        return when (hex.length) {
            6 -> (0xFF000000L or parsed).toInt()
            8 -> parsed.toInt()
            else -> fallback
        }
    }

    private fun applyAlpha(color: Int, opacity: Float): Int {
        val alpha = (opacity.coerceIn(0f, 1f) * 255f).roundToInt()
        return (color and 0x00FFFFFF) or (alpha shl 24)
    }

    private val DEFAULT_TIME_COLOR = 0xFFA78BFA.toInt()
    private val DEFAULT_DATE_COLOR = 0xFFA7A4B5.toInt()
}
