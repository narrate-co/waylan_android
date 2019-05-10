package space.narrate.words.android.data.disk.mw

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MwDao {

    @Transaction
    fun insert(word: MwWord, definitions: List<MwDefinitionGroup>) {
        insert(word)
        insertAll(*definitions.toTypedArray())
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(word: MwWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg definition: MwDefinitionGroup)

    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWord(word: String): MwWord?

    @Query("SELECT * FROM mw_definitions WHERE parentId = :word")
    fun getDefinitions(word: String): List<MwDefinitionGroup>

    @Transaction
    @Query("SELECT * FROM mw_words WHERE word = :word")
    fun getWordAndDefinitions(word: String): LiveData<List<MwWordAndDefinitionGroups>>

    @Query("DELETE FROM mw_words WHERE id = :id")
    fun deleteWord(id: String)

    @Query("DELETE FROM mw_definitions WHERE parentId = :parentId")
    fun deleteDefinitions(parentId: String)
}