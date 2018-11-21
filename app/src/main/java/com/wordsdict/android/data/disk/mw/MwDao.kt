package com.wordsdict.android.data.disk.mw

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MwDao {

    @Transaction
    fun insert(word: Word, definitions: List<Definition>) {
        insert(word)
        insertAll(*definitions.toTypedArray())
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: Word): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg definition: Definition): List<Long>

    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWord(word: String): Word?

    @Transaction
    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWordAndDefinitions(word: String): LiveData<List<WordAndDefinitions>>

    @Query("DELETE FROM mw_words WHERE id = :id")
    fun deleteWord(id: String)

    @Query("DELETE FROM mw_definitions WHERE parentId = :parentId")
    fun deleteDefinitions(parentId: String)
}