package com.words.android.data.disk

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.words.android.data.Meaning

@Dao
interface MeaningDao {

    @Insert
    fun insert(meaning: Meaning)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg meaning: Meaning)

    @Query("SELECT * FROM meanings WHERE parentWord = :word")
    fun getLive(word: String): LiveData<List<Meaning>>

    @Query("SELECT * FROM meanings WHERE parentWord = :word")
    fun get(word: String): List<Meaning>?

    @Query("DELETE FROM meanings")
    fun deleteAll()
}