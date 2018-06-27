package com.words.android.data

import androidx.room.Embedded
import androidx.room.Relation

data class WordAndMeanings(
        @Embedded
        var word: Word? = null,
        @Relation(parentColumn = "word", entityColumn = "parentWord")
        var meanings: List<Meaning> = ArrayList()
)

