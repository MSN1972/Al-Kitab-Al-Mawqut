package com.alkitabmawqut.tv

import org.junit.Assert.*
import org.junit.Test

class PostPrayerUnifiedContentTest {
    @Test fun allThreeCardsHaveTheirSources() {
        assertEquals(3, postPrayerPages.size)
        assertEquals(listOf("صحيح مسلم (591)", "صحيح البخاري (844)", "صحيح مسلم (597)"),
            postPrayerPages.map { it.source })
        assertTrue(postPrayerPages.all { it.title.isNotBlank() && it.text.isNotBlank() })
    }
    @Test fun includesFullTashbeehAndDua() {
        assertTrue(postPrayerPages[0].text.contains("أَسْتَغْفِرُ"))
        assertTrue(postPrayerPages[1].text.contains("لَا مَانِعَ"))
        assertTrue(postPrayerPages[2].text.contains("سُبْحَانَ"))
    }
}
