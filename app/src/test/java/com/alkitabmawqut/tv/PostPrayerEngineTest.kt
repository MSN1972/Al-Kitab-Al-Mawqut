package com.alkitabmawqut.tv

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PostPrayerEngineTest {
 private val date=LocalDate.of(2026,10,25)
 private val zone=ZoneId.of("Asia/Riyadh")
 private fun at(h:Int,m:Int,s:Int=0)=date.atTime(h,m,s).atZone(zone)
 private val events=PrayerEngine.events(date,listOf(300,720,930,1080,1170),listOf(15,15,5,5,15),listOf(7,7,5,5,7))
 @Test fun quietModeWins() {
  assertTrue(PrayerEngine.calculate(at(5,15),events,4).quiet)
  assertTrue(PrayerEngine.calculate(at(5,21,59),events,4).quiet)
 }
 @Test fun adhkarStartsAfterPrayer() {
  assertEquals(0,PrayerEngine.calculate(at(5,22),events,4).postPrayerIndex)
  assertFalse(PrayerEngine.calculate(at(5,22),events,4).quiet)
 }
 @Test fun returnsAfterFourMinutes() {
  assertEquals(0,PrayerEngine.calculate(at(5,25,59),events,4).postPrayerIndex)
  assertNull(PrayerEngine.calculate(at(5,26),events,4).postPrayerIndex)
 }
 @Test fun canDisableAdhkar() {assertNull(PrayerEngine.calculate(at(5,22),events,0).postPrayerIndex)}
 @Test fun crossesMidnight() {
  val e=PrayerEngine.events(date,listOf(300,720,930,1080,1438),listOf(15,15,5,5,6),listOf(7,7,5,5,7))
  val next=date.plusDays(1).atStartOfDay(zone)
  assertTrue(PrayerEngine.calculate(next.plusMinutes(5),e,4).quiet)
  assertEquals(4,PrayerEngine.calculate(next.plusMinutes(11),e,4).postPrayerIndex)
  assertNull(PrayerEngine.calculate(next.plusMinutes(15),e,4).postPrayerIndex)
 }
}
