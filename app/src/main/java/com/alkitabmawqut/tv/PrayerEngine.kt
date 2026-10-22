package com.alkitabmawqut.tv

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class PrayerEvent(
    val date: LocalDate,
    val prayerIndex: Int,
    val adhanMinute: Int,
    val iqamaMinute: Int,
    val quietMinutes: Int
) {
    fun iqamaEpochSecond(): Long = date.atStartOfDay(ZoneId.of("Asia/Riyadh")).toEpochSecond() + iqamaMinute * 60L
    fun quietEndEpochSecond(): Long = iqamaEpochSecond() + quietMinutes * 60L
}
data class ScreenState(val quiet: Boolean, val activePrayerIndex: Int?, val nextEvent: PrayerEvent?, val secondsUntilIqama: Long?)

object PrayerEngine {
    fun calculate(now: ZonedDateTime, events: List<PrayerEvent>): ScreenState {
        val second = now.toEpochSecond()
        val sorted = events.sortedBy { it.iqamaEpochSecond() }
        val active = sorted.lastOrNull { second >= it.iqamaEpochSecond() && second < it.quietEndEpochSecond() }
        if (active != null) return ScreenState(true, active.prayerIndex, null, null)
        val next = sorted.firstOrNull { it.iqamaEpochSecond() > second }
        return ScreenState(false, null, next, next?.iqamaEpochSecond()?.minus(second))
    }
    fun events(date: LocalDate, times: List<Int>, offsets: List<Int>, durations: List<Int>): List<PrayerEvent> {
        if (times.size != 5 || offsets.size != 5 || durations.size != 5) return emptyList()
        return (0..4).map { i -> PrayerEvent(date, i, times[i], times[i] + offsets[i], durations[i]) }
    }
}
