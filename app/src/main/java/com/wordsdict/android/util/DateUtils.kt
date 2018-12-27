package com.wordsdict.android.util

import org.threeten.bp.DateTimeUtils
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

private const val ONE_MINUTE_MILLIS = 60000L

val Date.isMoreThanOneMinuteAgo: Boolean
    get() {
        return Date().time - time > ONE_MINUTE_MILLIS
    }

fun Calendar.isSameDayAs(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
            get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}

val Date.daysElapsed: Long
    get() = ChronoUnit.DAYS.between(DateTimeUtils.toInstant(this), DateTimeUtils.toInstant(Date()))

val Date.minutesElapsed: Long
    get() = ChronoUnit.MINUTES.between(DateTimeUtils.toInstant(this), DateTimeUtils.toInstant(Date()))

val nearestMinute: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

val nearestHour: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

val nearestDay: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }


val nearestWeek: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.DAY_OF_WEEK, 0)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

val nearestMonth: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.DAY_OF_MONTH, 0)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }


val nearestYear: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.set(Calendar.MONTH, 0)
        cal.set(Calendar.DAY_OF_MONTH, 0)
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }


