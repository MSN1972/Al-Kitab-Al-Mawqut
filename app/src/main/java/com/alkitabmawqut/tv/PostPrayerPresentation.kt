package com.alkitabmawqut.tv

/** Three steady cards: 25% for istighfar, 25% for du'a, 50% for 33/33/33 dhikr.
 * The screen itself contains no animation; the caller supplies the elapsed time.
 */
object PostPrayerPresentation {
    fun pageAt(elapsedSeconds: Long, durationSeconds: Long): Int {
        if (durationSeconds <= 0L) return 0
        val elapsed = elapsedSeconds.coerceAtLeast(0L)
        return when {
            elapsed < durationSeconds / 4L -> 0
            elapsed < durationSeconds / 2L -> 1
            else -> 2
        }
    }
}
