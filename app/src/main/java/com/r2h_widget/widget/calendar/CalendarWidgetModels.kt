package com.r2h_widget.widget.calendar

/**
 * Legacy date-only calendar configuration.
 *
 * The calendar product is not part of the current six-product catalog,
 * but already-pinned home-screen calendar widgets keep rendering until their owners
 * remove them, so the configuration type stays available for stored records.
 */
data class CalendarWidgetConfiguration(
    val showWeekday: Boolean = true,
    val showMonth: Boolean = true,
)
