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



