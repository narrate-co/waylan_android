package com.words.android.data.disk.mw

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "mw_words")
data class Word(
        @PrimaryKey
        val word: String,
        val subj: String,
        val phonetic: String,
        val sound: Sound,
        val pronunciation: String,
        val partOfSpeech: String,
        val etymology: String,
        val uro: Uro
)