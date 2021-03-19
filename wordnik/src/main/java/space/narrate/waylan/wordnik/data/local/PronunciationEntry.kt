package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.data.remote.ApiPronunciation

@Entity(tableName = "wordnik_pronunciation")
data class PronunciationEntry(
  @PrimaryKey val id: String,
  val word: String,
  val pronunciations: List<Pronunciation>,
  val lastFetch: OffsetDateTime
) {
  companion object {
    fun fromRemote(word: String, apiPronunciations: List<ApiPronunciation>?): PronunciationEntry {
      return PronunciationEntry(
        word,
        word,
        apiPronunciations?.map {
          Pronunciation(
            it.id ?: "",
            it.seq ?: -1,
            it.raw ?: "",
            it.rawType ?: "",
            it.attributionText ?: "",
            it.attributionUrl ?: ""
          )
        } ?: emptyList(),
        OffsetDateTime.now()
      )
    }
  }
}

class Pronunciation(
  val id: String,
  val seq: Int,
  val raw: String,
  val rawType: String,
  val attributionText: String,
  val attributionUrl: String
)

