package com.alkitabmawqut.tv

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MobileEngineTest {
    @Test fun makkahDefaultsToCorrectZoneAndMethod() {
        assertEquals("Asia/Riyadh", PrayerLocation.MAKKAH.timezone)
        assertEquals(4, PrayerLocation.MAKKAH.method)
    }
    @Test fun supportsFivePrayersAndLocationTimeZone() {
        val date = LocalDate.of(2026,10,27)
        val schedules = PrayerEngine.events(date, listOf(305,725,925,1085,1165),
            listOf(15,15,15,5,15),listOf(7,7,5,5,7),"Asia/Riyadh")
        assertEquals(5,schedules.size)
        assertEquals("Asia/Riyadh", schedules[0].timezone)
        assertEquals(1180,schedules[4].iqamaMinute)
    }
    @Test fun azkarAllAvailableOnPhone() {
        assertEquals(3,postPrayerPages.size)
        assertTrue(postPrayerPages.all { it.source.isNotBlank() && it.text.isNotBlank() })
    }
}
