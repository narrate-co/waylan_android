package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.data.remote.ApiExamples

@Entity(tableName = "wordnik_examples")
data class ExampleEntry(
  @PrimaryKey val id: String,
  val word: String,
  val examples: List<Example>,
  val lastFetch: OffsetDateTime
) {
  companion object {
    fun fromRemote(word: String, apiExamples: ApiExamples?): ExampleEntry {
      return ExampleEntry(
        word,
        word,
        apiExamples?.examples?.map {
          Example(
            it.provider?.get("id") ?: 0,
            it.rating ?: 0F,
            it.url ?: "",
            it.word ?: word,
            it.text ?: "",
            it.documentId ?: 0L,
            it.exampleId ?: 0L,
            it.title ?: "",
            it.author ?: ""
          )
        } ?: emptyList(),
        OffsetDateTime.now()
      )
    }
  }
}

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