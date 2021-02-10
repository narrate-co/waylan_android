package space.narrate.waylan.merriamwebster_thesaurus.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ThesaurusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entry: ThesaurusEntry)

    @Query("SELECT * FROM thesaurus_entries WHERE word = :word")
    fun getWord(word: String): List<ThesaurusEntry>

    @Query("SELECT * FROM thesaurus_entries WHERE word = :word")
    fun getWordLive(word: String): LiveData<List<ThesaurusEntry>>
}