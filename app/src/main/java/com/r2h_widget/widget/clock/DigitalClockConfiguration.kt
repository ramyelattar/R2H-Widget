package com.r2h_widget.widget.clock

import com.r2h_widget.catalog.WidgetDefinition
import com.r2h_widget.catalog.WidgetProductIds
import com.r2h_widget.widget.common.WidgetActionConfiguration
import com.r2h_widget.widget.common.WidgetActionType
import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetEffectsConfiguration
import com.r2h_widget.widget.common.WidgetSizeClass

enum class ClockAccent(
    val label: String,
    val colorHex: String,
) {
    VIOLET("Violet", "#A78BFA"),
    CYAN("Cyan", "#38BDF8"),
    MINT("Mint", "#34D399"),
    AMBER("Amber", "#FBBF24"),
    ROSE("Rose", "#FB7185");

    companion object {
        fun fromHex(value: String): ClockAccent = entries.firstOrNull {
            it.colorHex.equals(value, ignoreCase = true)
        } ?: VIOLET
    }
}

enum class ClockTimeSeparator(val symbol: String) {
    COLON(":"),
    DOT("·"),
    SPACE(" "),
}

enum class ClockTimeAlignment {
    START,
    CENTER,
    END,
}

enum class ClockFontFamily(val label: String) {
    SYSTEM("System"),
    ROUNDED("Rounded"),
    CONDENSED("Condensed"),
    MONO("Mono"),
    ELEGANT("Elegant"),
    DIGITAL("Digital"),
}

enum class ClockDatePlacement {
    TOP,
    BOTTOM,
}

enum class ClockDateAlignment {
    START,
    CENTER,
    END,
}

enum class ClockWeekdayStyle {
    HIDDEN,
    SHORT,
    FULL,
}

enum class ClockMonthStyle {
    NUMERIC,
    SHORT_NAME,
    FULL_NAME,
}

enum class ClockYearStyle {
    HIDDEN,
    TWO_DIGIT,
    FOUR_DIGIT,
}

enum class ClockDateFormatPreset(val label: String) {
    DAY_MONTH_YEAR("21 May 2026"),
    MONTH_DAY_YEAR("May 21, 2026"),
    ISO("2026-05-21"),
    DAY_MONTH("21 May"),
    MONTH_DAY("May 21"),
}

data class ClockContentConfiguration(
    val use24HourFormat: Boolean = false,
    val showAmPm: Boolean = true,
    val leadingZero: Boolean = false,
    val separator: ClockTimeSeparator = ClockTimeSeparator.COLON,
    val showSeconds: Boolean = false,
    val alignment: ClockTimeAlignment = ClockTimeAlignment.CENTER,
)

data class ClockDateConfiguration(
    val enabled: Boolean = true,
    val placement: ClockDatePlacement = ClockDatePlacement.BOTTOM,
    val alignment: ClockDateAlignment = ClockDateAlignment.CENTER,
    val weekdayStyle: ClockWeekdayStyle = ClockWeekdayStyle.FULL,
    val monthStyle: ClockMonthStyle = ClockMonthStyle.SHORT_NAME,
    val yearStyle: ClockYearStyle = ClockYearStyle.FOUR_DIGIT,
    val formatPreset: ClockDateFormatPreset = ClockDateFormatPreset.DAY_MONTH_YEAR,
)

data class WidgetTypographyConfiguration(
    val timeFontFamily: ClockFontFamily = ClockFontFamily.SYSTEM,
    // Kept in persisted configurations for backward compatibility; weight editing remains
    // unavailable until every bundled family has verified launcher-safe weight support.
    val timeFontWeight: Int = 500,
    val timeScale: Float = 1f,
    val timeColorHex: String = ClockAccent.VIOLET.colorHex,
    val letterSpacing: Float = 0f,
    val dateFontFamily: ClockFontFamily = ClockFontFamily.SYSTEM,
    val dateFontWeight: Int = 400,
    val dateScale: Float = 0.34f,
    val dateColorHex: String = "#FFFFFF",
    val dateOpacity: Float = 0.72f,
)

data class WidgetLayoutConfiguration(
    val contentPaddingDp: Float = 16f,
    val horizontalOffsetDp: Float = 0f,
    val verticalOffsetDp: Float = 0f,
    val timeDateGapDp: Float = 6f,
    val alignment: ClockTimeAlignment = ClockTimeAlignment.CENTER,
)

data class ResponsiveWidgetConfiguration(
    val compactScale: Float = 0.82f,
    val mediumScale: Float = 1f,
    val expandedScale: Float = 1.16f,
    val compactShowDate: Boolean = true,
)

/**
 * Canonical typed configuration for the Digital Clock product and each of its widget
 * instances. The scalar properties are read-only compatibility projections kept for
 * persisted-record callers.
 */
data class DigitalClockConfiguration(
    val content: ClockContentConfiguration,
    val date: ClockDateConfiguration,
    val background: WidgetBackgroundConfiguration = WidgetBackgroundConfiguration(),
    val typography: WidgetTypographyConfiguration = WidgetTypographyConfiguration(),
    val layout: WidgetLayoutConfiguration = WidgetLayoutConfiguration(),
    val effects: WidgetEffectsConfiguration = WidgetEffectsConfiguration(),
    val action: WidgetActionConfiguration = WidgetActionConfiguration(),
    val responsive: ResponsiveWidgetConfiguration = ResponsiveWidgetConfiguration(),
    val presetId: String? = null,
) {
    /** Compatibility constructor for the original foundation persistence/API shape. */
    constructor(
        use24HourFormat: Boolean = false,
        showDate: Boolean = true,
        showWeekday: Boolean = true,
        accent: ClockAccent = ClockAccent.VIOLET,
    ) : this(
        content = ClockContentConfiguration(use24HourFormat = use24HourFormat),
        date = ClockDateConfiguration(
            enabled = showDate,
            weekdayStyle = if (showWeekday) ClockWeekdayStyle.FULL else ClockWeekdayStyle.HIDDEN,
        ),
        typography = WidgetTypographyConfiguration(
            timeColorHex = accent.colorHex,
            dateColorHex = "#FFFFFF",
        ),
        effects = WidgetEffectsConfiguration(glowColorHex = accent.colorHex),
        background = WidgetBackgroundConfiguration(highlightColorHex = accent.colorHex),
    )

    val use24HourFormat: Boolean
        get() = content.use24HourFormat

    val showDate: Boolean
        get() = date.enabled

    val showWeekday: Boolean
        get() = date.weekdayStyle != ClockWeekdayStyle.HIDDEN

    val accent: ClockAccent
        get() = ClockAccent.fromHex(typography.timeColorHex)

    fun with24HourFormat(enabled: Boolean): DigitalClockConfiguration = copy(
        content = content.copy(use24HourFormat = enabled),
    )

    fun withShowDate(enabled: Boolean): DigitalClockConfiguration = copy(
        date = date.copy(enabled = enabled),
    )

    fun withShowWeekday(enabled: Boolean): DigitalClockConfiguration = copy(
        date = date.copy(
            weekdayStyle = if (enabled) {
                date.weekdayStyle.takeUnless { it == ClockWeekdayStyle.HIDDEN } ?: ClockWeekdayStyle.FULL
            } else {
                ClockWeekdayStyle.HIDDEN
            },
        ),
    )

    fun withAccent(accent: ClockAccent): DigitalClockConfiguration = copy(
        typography = typography.copy(timeColorHex = accent.colorHex),
        effects = effects.copy(glowColorHex = accent.colorHex),
        background = background.copy(highlightColorHex = accent.colorHex),
    )
}

typealias ClockWidgetConfiguration = DigitalClockConfiguration

/**
 * Every digital clock starts from the same strong default; the studio's style
 * presets are the starting points users used to get from duplicate catalog entries.
 */
fun WidgetDefinition.defaultClockConfiguration(): DigitalClockConfiguration = when (id) {
    WidgetProductIds.DIGITAL_CLOCK -> DigitalClockConfiguration(
        accent = ClockAccent.fromHex(previewAccentHex),
    ).copy(
        typography = WidgetTypographyConfiguration(
            timeColorHex = previewAccentHex,
        ),
        effects = WidgetEffectsConfiguration(glowColorHex = previewAccentHex),
        background = WidgetBackgroundConfiguration(highlightColorHex = previewAccentHex),
    )
    else -> DigitalClockConfiguration()
}
