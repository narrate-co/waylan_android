package space.narrate.waylan.android.util

import org.threeten.bp.DateTimeUtils
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

private const val ONE_MINUTE_MILLIS = 60000L

val Date.isMoreThanOneMinuteAgo: Boolean
    get() {
        return Date().time - time > ONE_MINUTE_MILLIS
    }

val Date.daysElapsed: Long
    get() = ChronoUnit.DAYS.between(DateTimeUtils.toInstant(this), DateTimeUtils.toInstant(Date()))


