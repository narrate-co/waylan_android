package com.words.android.data

import org.threeten.bp.OffsetDateTime


data class Synonym(
        val synonym: String,
        val created: OffsetDateTime,
        val modified: OffsetDateTime
)

