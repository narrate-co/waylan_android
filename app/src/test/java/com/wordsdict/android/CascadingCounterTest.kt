package com.wordsdict.android

import com.wordsdict.android.data.firestore.words.GlobalWord
import com.wordsdict.android.util.CascadingCounter
import com.wordsdict.android.util.daysElapsed
import org.junit.Test
import java.util.*
import org.junit.Assert.*

class CascadingCounterTest {


    private fun newGlobalWord(): GlobalWord {
        return GlobalWord("quiescent", "quiescent")
    }

    @Test
    fun allViewCounts_shouldBeOne() {
        val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)
    }

    @Test
    fun allViewCounts_shouldBeTwo() {
        val word = newGlobalWord()
        CascadingCounter.plus(word, 1)
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(2,2,2,2,2,2)
    }

    @Test
    fun shouldResetMinute() {
        val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoMinutesAgo = word.minuteViewCountStarted.addMinutes(-2)
        word.minuteViewCountStarted = twoMinutesAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,2,2,2,2,2)
    }

    @Test
    fun shouldResetMinuteAndHour() {
         val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoHoursAgo = word.hourViewCountStarted.addHours(-2)
        word.minuteViewCountStarted = twoHoursAgo
        word.hourViewCountStarted = twoHoursAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,2,2,2,2)
    }

    @Test
    fun shouldResetMinuteAndHourAndDay() {
         val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoDaysAgo = word.hourViewCountStarted.addDays(-2)
        word.minuteViewCountStarted = twoDaysAgo
        word.hourViewCountStarted = twoDaysAgo
        word.dayViewCountStarted = twoDaysAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,2,2,2)
    }

    @Test
    fun shouldResetMinuteAndHourAndDayAndWeek() {
        val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoWeeksAgo = word.hourViewCountStarted.addDays(-14)
        word.minuteViewCountStarted = twoWeeksAgo
        word.hourViewCountStarted = twoWeeksAgo
        word.dayViewCountStarted = twoWeeksAgo
        word.weekViewCountStarted = twoWeeksAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,2,2)
    }

    @Test
    fun shouldResetMinuteAndHourAndDayAndWeekAndMonth() {
        val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoMonthsAgo = word.monthViewCountStarted.addMonths(-2)
        word.minuteViewCountStarted = twoMonthsAgo
        word.hourViewCountStarted = twoMonthsAgo
        word.dayViewCountStarted = twoMonthsAgo
        word.weekViewCountStarted = twoMonthsAgo
        word.monthViewCountStarted = twoMonthsAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,2)
    }

    @Test
    fun shouldResetMinuteAndHourAndDayAndWeekAndMonthAndYear() {
         val word = newGlobalWord()
        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)

        val twoYearsAgo = word.yearViewCountStarted.addYears(-2)
        word.minuteViewCountStarted = twoYearsAgo
        word.hourViewCountStarted = twoYearsAgo
        word.dayViewCountStarted = twoYearsAgo
        word.weekViewCountStarted = twoYearsAgo
        word.monthViewCountStarted = twoYearsAgo
        word.yearViewCountStarted = twoYearsAgo

        CascadingCounter.plus(word, 1)

        word.assertViewCountsEquals(1,1,1,1,1,1)
    }

    private fun GlobalWord.assertViewCountsEquals(
            minute: Long,
            hour: Long,
            day: Long,
            week: Long,
            month: Long,
            year: Long
    ) {

        assertEquals(minute, minuteViewCount)
        assertEquals(hour, hourViewCount)
        assertEquals(day, dayViewCount)
        assertEquals(week, weekViewCount)
        assertEquals(month, monthViewCount)
        assertEquals(year, yearViewCount)

    }

}