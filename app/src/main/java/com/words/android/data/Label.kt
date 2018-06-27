package com.words.android.data

import org.threeten.bp.OffsetDateTime


data class Label(
        val name: String,
        val isDialect: Boolean,
        val created: OffsetDateTime,
        val modified: OffsetDateTime
)

