package com.words.android.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.words.android.data.Meaning
import com.words.android.data.Word
import com.words.android.data.WordAndMeanings
import com.words.android.data.disk.AppDatabase
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.OffsetDateTime

class WordRepository(private val db: AppDatabase) {


    fun insertWord(word: String) = launch {
        db.wordDao().insert(Word(
                word,
                0,
                OffsetDateTime.now(),
                OffsetDateTime.now()))
    }

    fun filterWords(query: String): LiveData<List<Word>> =
            db.wordDao().load("$query%")

    fun getWordLive(word: String): LiveData<Word> =
            db.wordDao().getLive(word)

    fun getWord(word: String): Deferred<Word?> = async {
        db.wordDao().get(word)
    }

    fun getMeaningsLive(word: String): LiveData<List<Meaning>> =
            db.meaningDao().getLive(word)

    fun getMeanings(word: String): Deferred<List<Meaning>?> = async {
        db.meaningDao().get(word)
    }

    fun getWordAndMeanings(word: String): LiveData<WordAndMeanings> =
            db.wordDao().getWordAndMeanings(word)

}

