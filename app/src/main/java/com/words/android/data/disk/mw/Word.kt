package com.words.android.data.disk.mw

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "mw_words")
data class Word(
        @PrimaryKey
        val id: String,
        val word: String,
        val subj: String,
        val phonetic: String,
        val sound: Sound,
        val pronunciation: String,
        val partOfSpeech: String,
        val etymology: String,
        val uro: Uro
) {
        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other !is Word) return false
            if (this === other) return true

            return id == other.id &&
                    word == other.word &&
                    subj == other.subj &&
                    phonetic == other.phonetic &&
                    sound == other.sound &&
                    pronunciation == other.pronunciation &&
                    partOfSpeech == other.partOfSpeech &&
                    etymology == other.etymology
//            Uro not included!
        }

    override fun toString(): String {
        return "$id, $word, $subj, $phonetic, $sound, $pronunciation, $partOfSpeech, $etymology"
    }
}