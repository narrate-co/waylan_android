package com.words.android.util

import org.threeten.bp.OffsetDateTime
import java.util.*

private const val ONE_MINUTE_MILLIS = 60000L

val OffsetDateTime.toDate
    get() = Date(toInstant().toEpochMilli())

val Date.isMoreThanOneMinuteAgo: Boolean
    get() {
        val dateInMillis = time
        return Date().time - time > ONE_MINUTE_MILLIS
    }