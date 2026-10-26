package com.alkitabmawqut.tv

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class LocationScheduleTest {
    @Test fun makkahDefaultRemainsUmmAlQura() {
        assertEquals("Asia/Riyadh", PrayerLocation.MAKKAH.timezone)
        assertEquals(4,PrayerLocation.MAKKAH.method)
    }
    @Test fun clockZonesYieldDifferentInstants() {
        val d=LocalDate.of(2026,7,1)
        val x=listOf(300,720,930,1080,1170)
        val a=PrayerEngine.events(d,x,List(5){15},List(5){7},"Asia/Riyadh")
        val b=PrayerEngine.events(d,x,List(5){15},List(5){7},"Europe/London")
        assertNotEquals(a[0].iqamaEpochSecond(),b[0].iqamaEpochSecond())
    }
    @Test fun dstMidnightConversionUsesLocalTime() {
        val d=LocalDate.of(2026,3,29)
        val x=listOf(300,720,930,1080,1170)
        val events=PrayerEngine.events(d,x,List(5){15},List(5){7},"Europe/London")
        assertEquals(d.atTime(5,15).atZone(ZoneId.of("Europe/London")).toEpochSecond(),
            events[0].iqamaEpochSecond())
    }
    @Test fun cacheSeparatesCitiesAndMethods() {
        val base=PrayerLocation.MAKKAH
        assertNotEquals(base.cacheKey(),base.copy(id=7).cacheKey())
        assertNotEquals(base.cacheKey(),base.copy(method=3).cacheKey())
    }
    @Test fun rejectsInvalidZoneInSettings() {
        assertNull(PrayerLocation.decode(PrayerLocation.MAKKAH.encode().replace("Asia/Riyadh","Fake/Zone")))
    }
}