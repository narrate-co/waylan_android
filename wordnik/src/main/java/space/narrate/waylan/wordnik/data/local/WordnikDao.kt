package space.narrate.waylan.wordnik.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordnikDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(definitionEntry: DefinitionEntry)

  @Query("SELECT * FROM wordnik_definitions WHERE word = :word")
  fun getDefinitionEntry(word: String): Flow<DefinitionEntry?>

  @Query("SELECT * FROM wordnik_definitions WHERE word = :word")
  suspend fun getDefinitionEntryImmediate(word: String): DefinitionEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(exampleEntry: ExampleEntry)

  @Query("SELECT * FROM wordnik_examples WHERE word = :word")
  fun getExampleEntry(word: String): Flow<ExampleEntry?>

  @Query("SELECT * FROM wordnik_examples WHERE word = :word")
  suspend fun getExampleEntryImmediate(word: String): ExampleEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(audioEntry: AudioEntry)

  @Query("SELECT * FROM wordnik_audio WHERE word = :word")
  fun getAudioEntry(word: String): Flow<AudioEntry?>

  @Query("SELECT * FROM wordnik_audio WHERE word = :word")
  suspend fun getAudioEntryImmediate(word: String): AudioEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(frequencyEntry: FrequencyEntry)

  @Query("SELECT * FROM wordnik_frequency WHERE word = :word")
  fun getFrequencyEntry(word: String): Flow<FrequencyEntry?>

  @Query("SELECT * FROM wordnik_frequency WHERE word = :word")
  suspend fun getFrequencyEntryImmediate(word: String): FrequencyEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(hyphenationEntry: HyphenationEntry)

  @Query("SELECT * FROM wordnik_hyphenation WHERE word = :word")
  fun getHyphenationEntry(word: String): Flow<HyphenationEntry?>

  @Query("SELECT * FROM wordnik_hyphenation WHERE word = :word")
  suspend fun getHyphenationEntryImmediate(word: String): HyphenationEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(pronunciationEntry: PronunciationEntry)

  @Query("SELECT * FROM wordnik_pronunciation WHERE word = :word")
  fun getPronunciationEntry(word: String): Flow<PronunciationEntry?>

  @Query("SELECT * FROM wordnik_pronunciation WHERE word = :word")
  suspend fun getPronunciationEntryImmediate(word: String): PronunciationEntry?

}