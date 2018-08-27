package com.words.android.data.disk.mw

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
    fun insert(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg definition: Definition)

    @Transaction
    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWordAndDefinitions(word: String): LiveData<WordAndDefinitions>

    @Query("DELETE FROM mw_words WHERE word = :word")
    fun deleteWord(word: String)

    @Query("DELETE FROM mw_definitions WHERE parentWord = :parentWord")
    fun deleteDefinitions(parentWord: String)
}