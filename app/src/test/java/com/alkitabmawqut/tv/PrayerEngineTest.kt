package com.alkitabmawqut.tv

import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.ZoneId

class PrayerEngineTest {
 private val date = LocalDate.of(2026, 10, 22)
 private val zone = ZoneId.of("Asia/Riyadh")
 private val times = listOf(300, 720, 930, 1080, 1170)
 private val offsets = listOf(15, 15, 5, 5, 15)
 private val quiet = listOf(7, 7, 5, 5, 7)
 private fun at(d: LocalDate, h: Int, m: Int, s: Int = 0) = d.atTime(h,m,s).atZone(zone)
 private fun events(d: LocalDate = date) = PrayerEngine.events(d, times, offsets, quiet)
 @Test fun beforeIqama() { assertEquals(60L, PrayerEngine.calculate(at(date,5,14), events()).secondsUntilIqama) }
 @Test fun quietAtIqama() { assertTrue(PrayerEngine.calculate(at(date,5,15), events()).quiet) }
 @Test fun quietThroughLastSecond() { assertTrue(PrayerEngine.calculate(at(date,5,21,59), events()).quiet) }
 @Test fun returnsAfterQuiet() { assertFalse(PrayerEngine.calculate(at(date,5,22), events()).quiet) }
 @Test fun handlesTomorrow() { assertEquals(date.plusDays(1), PrayerEngine.calculate(at(date,22,55), events()+events(date.plusDays(1))).nextEvent?.date) }
 @Test fun missingTomorrow() { assertNull(PrayerEngine.calculate(at(date,22,55), events()).nextEvent) }
 @Test fun afterMidnight() {
  val late = PrayerEngine.events(date, listOf(300,720,930,1080,1438), listOf(15,15,5,5,6), quiet)
  assertTrue(PrayerEngine.calculate(at(date.plusDays(1),0,5), late).quiet)
 }
 @Test fun customDuration() {
  val custom = PrayerEngine.events(date,times,offsets,listOf(7,7,6,5,7))
  assertFalse(PrayerEngine.calculate(at(date,15,41),custom).quiet)
 }
 @Test fun invalidSize() { assertTrue(PrayerEngine.events(date,listOf(1),offsets,quiet).isEmpty()) }
 @Test fun emptySchedule() { assertNull(PrayerEngine.calculate(at(date,12,0), emptyList()).nextEvent) }
}
