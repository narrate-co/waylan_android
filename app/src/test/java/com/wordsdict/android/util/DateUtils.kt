package com.wordsdict.android.util

import com.wordsdict.android.addMinutes
import org.junit.Test
import org.junit.Assert.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.temporal.ChronoUnit

class DateUtils {

    @Test
    fun nearestMinute_isLessThanOneMinuteAgo() {
        val nm = nearestMinute

        assert(nm.minutesElapsed < 1L)
    }

    @Test
    fun nearestMinute_isEqualToZeroMinutesAgo() {
        val nm = nearestMinute

        assert(nm.minutesElapsed == 0L)
    }

    @Test
    fun nearestHour_isLessSixtyMinutesAgo() {
        val nh = nearestHour

        assert(nh.minutesElapsed < 60L)
    }

    @Test
    fun addMinutes_shouldBeOneMinuteElapsed() {
        val nm = nearestMinute
        val nextMinute = nm.addMinutes(1)
        val elapsed = ChronoUnit.MINUTES.between(DateTimeUtils.toInstant(nm), DateTimeUtils.toInstant(nextMinute))
        assertEquals(1L, elapsed)
    }
}