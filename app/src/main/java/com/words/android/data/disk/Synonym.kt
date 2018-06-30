package com.words.android.data.disk

import org.threeten.bp.OffsetDateTime


data class Synonym(
        val synonym: String,
        val created: OffsetDateTime,
        val modified: OffsetDateTime
)

