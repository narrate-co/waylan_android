package com.words.android.data.disk.mw

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MwDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg definition: Definition)

    @Transaction
    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWordAndDefinitions(word: String): LiveData<WordAndDefinitions>
}