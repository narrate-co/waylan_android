package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "wordnik_examples")
data class ExampleEntry(
  @PrimaryKey val id: String,
  val word: String,
  val examples: List<Example>,
  val lastFetch: OffsetDateTime
)

class Example(
  val providerId: Int,
  val rating: Float,
  val url: String,
  val word: String,
  val text: String,
  val documentId: Long,
  val exampleId: Long,
  val title: String,
  val author: String
)