package com.words.android.util

import org.threeten.bp.OffsetDateTime
import java.util.*


val OffsetDateTime.toDate
    get() = Date(toInstant().toEpochMilli())