package com.alkitabmawqut.tv

import java.time.LocalDate
import java.time.YearMonth

/** Ensure the previous day's prayer and post-prayer transition work across month boundaries. */
object ScheduleSupport {
    fun monthsFor(day: LocalDate): List<YearMonth> =
        listOf(day.minusDays(1), day, day.plusDays(1))
            .map(YearMonth::from).distinct()
}
