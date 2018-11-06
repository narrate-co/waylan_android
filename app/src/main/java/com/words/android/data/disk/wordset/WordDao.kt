package com.words.android.data.disk.wordset

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WordDao {

    @Insert
    fun insert(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg word: Word)

    @Query("SELECT * FROM words WHERE word = :word")
    fun getLive(word: String): LiveData<Word>

    @Query("SELECT * FROM words WHERE word = :word")
    fun get(word: String): Word?

    @Transaction
    @Query("SELECT * FROM words WHERE word = :word ORDER BY word")
    fun getWordAndMeanings(word: String): LiveData<WordAndMeanings>

    @Query("SELECT * FROM words ORDER BY word ASC")
    fun getAll(): LiveData<List<Word>>

    @Query("SELECT * FROM words WHERE word LIKE :query COLLATE NOCASE LIMIT 25")
    fun load(query: String): LiveData<List<Word>>

    @Query("DELETE FROM words")
    fun deleteAll()
}

