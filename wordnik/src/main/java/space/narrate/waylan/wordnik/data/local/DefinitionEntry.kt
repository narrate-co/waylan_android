package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "wordnik_definitions")
data class DefinitionEntry(
  @PrimaryKey val id: String,
  val word: String,
  val definitions: List<Definition>,
  val lastFetch: OffsetDateTime
)

class Definition(
  val id: String,
  val partOfSpeech: String,
  val attributionText: String,
  val sourceDictionary: String,
  val text: String,
  val sequence: String,
  val score: Int,
  val labels: List<Map<String ,String>>,
  val citations: List<Map<String, String>>,
  val word: String,
  val relatedWords: List<Map<String, String>>,
  val exampleUses: List<Map<String, String>>,
  val textProns: List<Map<String, String>>,
  val notes: List<Map<String, String>>,
  val attributionUrl: String,
  val wordnikUrl: String
)