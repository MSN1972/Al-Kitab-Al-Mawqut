package com.alkitabmawqut.tv

import org.junit.Assert.assertEquals
import org.junit.Test

class PostPrayerPresentationTest {
    @Test fun fourMinuteSequence() {
        assertEquals(0, PostPrayerPresentation.pageAt(0, 240))
        assertEquals(0, PostPrayerPresentation.pageAt(59, 240))
        assertEquals(1, PostPrayerPresentation.pageAt(60, 240))
        assertEquals(1, PostPrayerPresentation.pageAt(119, 240))
        assertEquals(2, PostPrayerPresentation.pageAt(120, 240))
        assertEquals(2, PostPrayerPresentation.pageAt(239, 240))
    }
    @Test fun sixtySecondSequence() {
        assertEquals(0, PostPrayerPresentation.pageAt(0, 60))
        assertEquals(1, PostPrayerPresentation.pageAt(15, 60))
        assertEquals(2, PostPrayerPresentation.pageAt(30, 60))
    }
    @Test fun badInputsCannotBreakPage() {
        assertEquals(0, PostPrayerPresentation.pageAt(0, 0))
        assertEquals(0, PostPrayerPresentation.pageAt(-40, 240))
        assertEquals(2, PostPrayerPresentation.pageAt(900, 240))
    }
}
