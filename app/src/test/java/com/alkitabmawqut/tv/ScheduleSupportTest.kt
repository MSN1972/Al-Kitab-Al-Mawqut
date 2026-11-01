package com.alkitabmawqut.tv

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class ScheduleSupportTest {
    @Test fun middleOfMonthLoadsOnlyCurrentMonth() {
        assertEquals(listOf(YearMonth.of(2026, 11)),
            ScheduleSupport.monthsFor(LocalDate.of(2026, 11, 15)))
    }

    @Test fun beginningOfMonthRetainsPreviousPrayers() {
        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
            ScheduleSupport.monthsFor(LocalDate.of(2026, 11, 1)))
    }

    @Test fun endOfYearIncludesNextYear() {
        assertEquals(listOf(YearMonth.of(2026, 12), YearMonth.of(2027, 1)),
            ScheduleSupport.monthsFor(LocalDate.of(2026, 12, 31)))
    }
}
