package com.words.android.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime


@Entity(tableName = "words")
data class Word(
        @PrimaryKey
        val word: String,
        val popularity: Int,
        val created: OffsetDateTime,
        val modified: OffsetDateTime
)

