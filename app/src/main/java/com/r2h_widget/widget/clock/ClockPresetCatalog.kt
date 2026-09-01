package com.r2h_widget.widget.clock

import com.r2h_widget.widget.common.WidgetBackgroundConfiguration
import com.r2h_widget.widget.common.WidgetBackgroundFill
import com.r2h_widget.widget.common.WidgetEffectsConfiguration

data class ClockWidgetPreset(
    val id: String,
    val name: String,
    val configuration: DigitalClockConfiguration,
)

object ClockPresetCatalog {
    val builtIn: List<ClockWidgetPreset> = listOf(
        ClockWidgetPreset(
            id = "midnight-glass",
            name = "Midnight Glass",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(
                    use24HourFormat = true,
                    leadingZero = true,
                    alignment = ClockTimeAlignment.CENTER,
                ),
                date = ClockDateConfiguration(
                    weekdayStyle = ClockWeekdayStyle.SHORT,
                    monthStyle = ClockMonthStyle.SHORT_NAME,
                    yearStyle = ClockYearStyle.HIDDEN,
                    formatPreset = ClockDateFormatPreset.DAY_MONTH,
                ),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.LINEAR_GRADIENT,
                    gradientStartHex = "#171229",
                    gradientEndHex = "#090812",
                    gradientAngleDegrees = 135f,
                    borderEnabled = true,
                    borderColorHex = "#A78BFA",
                    borderOpacity = 0.16f,
                    highlightColorHex = "#6D5CCF",
                    highlightOpacity = 0.18f,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.SYSTEM,
                    timeFontWeight = 600,
                    timeScale = 1.08f,
                    timeColorHex = "#F4F2FA",
                    dateColorHex = "#B8B0D3",
                    dateScale = 0.28f,
                ),
                layout = WidgetLayoutConfiguration(
                    contentPaddingDp = 20f,
                    timeDateGapDp = 8f,
                ),
                presetId = "midnight-glass",
            ),
        ),
        ClockWidgetPreset(
            id = "minimal-amoled",
            name = "Minimal AMOLED",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(
                    use24HourFormat = true,
                    leadingZero = true,
                ),
                date = ClockDateConfiguration(
                    weekdayStyle = ClockWeekdayStyle.SHORT,
                    monthStyle = ClockMonthStyle.SHORT_NAME,
                    yearStyle = ClockYearStyle.HIDDEN,
                    formatPreset = ClockDateFormatPreset.DAY_MONTH,
                ),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.TRANSPARENT,
                    borderEnabled = false,
                    highlightEnabled = false,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.SYSTEM,
                    timeFontWeight = 700,
                    timeScale = 1.16f,
                    timeColorHex = "#FFFFFF",
                    dateColorHex = "#A7A4B5",
                    dateScale = 0.25f,
                ),
                layout = WidgetLayoutConfiguration(
                    contentPaddingDp = 20f,
                    timeDateGapDp = 5f,
                ),
                presetId = "minimal-amoled",
            ),
        ),
        ClockWidgetPreset(
            id = "neon-pulse",
            name = "Neon Pulse",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(
                    use24HourFormat = true,
                    leadingZero = true,
                    alignment = ClockTimeAlignment.CENTER,
                ),
                date = ClockDateConfiguration(weekdayStyle = ClockWeekdayStyle.SHORT),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.RADIAL_GRADIENT,
                    gradientStartHex = "#101A2A",
                    gradientEndHex = "#05070D",
                    highlightColorHex = ClockAccent.CYAN.colorHex,
                    highlightOpacity = 0.22f,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.MONO,
                    timeColorHex = ClockAccent.CYAN.colorHex,
                    letterSpacing = 0.06f,
                ),
                effects = WidgetEffectsConfiguration(
                    glowEnabled = true,
                    glowColorHex = ClockAccent.CYAN.colorHex,
                    glowStrength = 0.35f,
                ),
                presetId = "neon-pulse",
            ),
        ),
        ClockWidgetPreset(
            id = "solar-amber",
            name = "Solar Amber",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(use24HourFormat = true),
                date = ClockDateConfiguration(),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.LINEAR_GRADIENT,
                    gradientStartHex = "#25180A",
                    gradientEndHex = "#0D0905",
                    highlightColorHex = ClockAccent.AMBER.colorHex,
                    highlightOpacity = 0.2f,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.ROUNDED,
                    timeColorHex = ClockAccent.AMBER.colorHex,
                ),
                effects = WidgetEffectsConfiguration(
                    glowEnabled = true,
                    glowColorHex = ClockAccent.AMBER.colorHex,
                    glowStrength = 0.24f,
                ),
                presetId = "solar-amber",
            ),
        ),
        ClockWidgetPreset(
            id = "mint-terminal",
            name = "Mint Terminal",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(
                    use24HourFormat = true,
                    leadingZero = true,
                    separator = ClockTimeSeparator.DOT,
                ),
                date = ClockDateConfiguration(
                    weekdayStyle = ClockWeekdayStyle.SHORT,
                    formatPreset = ClockDateFormatPreset.ISO,
                ),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.SOLID,
                    solidColorHex = "#07130F",
                    highlightColorHex = ClockAccent.MINT.colorHex,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.DIGITAL,
                    timeColorHex = ClockAccent.MINT.colorHex,
                    dateColorHex = ClockAccent.MINT.colorHex,
                ),
                presetId = "mint-terminal",
            ),
        ),
        ClockWidgetPreset(
            id = "rose-minimal",
            name = "Rose Minimal",
            configuration = DigitalClockConfiguration(
                content = ClockContentConfiguration(use24HourFormat = false),
                date = ClockDateConfiguration(
                    weekdayStyle = ClockWeekdayStyle.HIDDEN,
                    formatPreset = ClockDateFormatPreset.DAY_MONTH,
                ),
                background = WidgetBackgroundConfiguration(
                    fill = WidgetBackgroundFill.TRANSPARENT,
                    borderEnabled = true,
                    borderColorHex = ClockAccent.ROSE.colorHex,
                    borderOpacity = 0.38f,
                    highlightColorHex = ClockAccent.ROSE.colorHex,
                    highlightOpacity = 0.08f,
                ),
                typography = WidgetTypographyConfiguration(
                    timeFontFamily = ClockFontFamily.ELEGANT,
                    timeColorHex = ClockAccent.ROSE.colorHex,
                ),
                layout = WidgetLayoutConfiguration(contentPaddingDp = 20f),
                presetId = "rose-minimal",
            ),
        ),
    )

    fun findById(id: String): ClockWidgetPreset? = builtIn.firstOrNull { it.id == id }
}
