package com.r2h_widget.widget.clock

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ClockDisplay(
    val time: String,
    val date: String,
    val weekday: String,
)

object ClockFormatter {
    fun format(
        instant: Instant,
        configuration: DigitalClockConfiguration,
        zoneId: ZoneId,
        locale: Locale,
    ): ClockDisplay {
        val zonedDateTime = instant.atZone(zoneId)
        val timePattern = timePattern(configuration)
        val datePattern = datePattern(configuration.date)
        val weekdayPattern = weekdayPattern(configuration.date)
        return ClockDisplay(
            time = DateTimeFormatter.ofPattern(timePattern, locale).format(zonedDateTime),
            date = DateTimeFormatter.ofPattern(datePattern, locale).format(zonedDateTime),
            weekday = DateTimeFormatter.ofPattern(weekdayPattern, locale).format(zonedDateTime),
        )
    }

    fun now(configuration: DigitalClockConfiguration): ClockDisplay = format(
        instant = Instant.now(),
        configuration = configuration,
        zoneId = ZoneId.systemDefault(),
        locale = Locale.getDefault(),
    )

    fun timePattern(configuration: DigitalClockConfiguration): String {
        val content = configuration.content
        val hour = if (content.use24HourFormat) "HH" else if (content.leadingZero) "hh" else "h"
        val separator = when (content.separator) {
            ClockTimeSeparator.COLON -> ":"
            ClockTimeSeparator.DOT -> "."
            ClockTimeSeparator.SPACE -> " "
        }
        val seconds = if (content.showSeconds) "${separator}ss" else ""
        val amPm = if (!content.use24HourFormat && content.showAmPm) " a" else ""
        return hour + separator + "mm" + seconds + amPm
    }

    fun datePattern(configuration: ClockDateConfiguration): String {
        val date = configuration
        return when (date.formatPreset) {
            ClockDateFormatPreset.DAY_MONTH_YEAR -> buildPattern("d", date.monthStyle, date.yearStyle, " ")
            ClockDateFormatPreset.MONTH_DAY_YEAR -> buildPattern("d", date.monthStyle, date.yearStyle, ", ", monthFirst = true)
            ClockDateFormatPreset.ISO -> "yyyy-MM-dd"
            ClockDateFormatPreset.DAY_MONTH -> buildPattern("d", date.monthStyle, ClockYearStyle.HIDDEN, " ")
            ClockDateFormatPreset.MONTH_DAY -> buildPattern("d", date.monthStyle, ClockYearStyle.HIDDEN, " ", monthFirst = true)
        }
    }

    fun weekdayPattern(configuration: ClockDateConfiguration): String =
        if (configuration.weekdayStyle == ClockWeekdayStyle.SHORT) "EEE" else "EEEE"

    private fun buildPattern(
        day: String,
        monthStyle: ClockMonthStyle,
        yearStyle: ClockYearStyle,
        yearSeparator: String,
        monthFirst: Boolean = false,
    ): String {
        val month = when (monthStyle) {
            ClockMonthStyle.NUMERIC -> "M"
            ClockMonthStyle.SHORT_NAME -> "MMM"
            ClockMonthStyle.FULL_NAME -> "MMMM"
        }
        val year = when (yearStyle) {
            ClockYearStyle.HIDDEN -> ""
            ClockYearStyle.TWO_DIGIT -> "yy"
            ClockYearStyle.FOUR_DIGIT -> "yyyy"
        }
        val dayMonth = if (monthFirst) "$month $day" else "$day $month"
        return if (year.isBlank()) dayMonth else "$dayMonth$yearSeparator$year"
    }
}
