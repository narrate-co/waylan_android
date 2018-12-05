package com.wordsdict.android.data.disk.mw

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A Room convenience class to join a [Word] and all its child [Definition]s with a single query
 */
data class WordAndDefinitions(
        @Embedded
        var word: Word? = null,
        @Relation(parentColumn = "id", entityColumn = "parentId")
        var definitions: List<Definition> = ArrayList()
)

