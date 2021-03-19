package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.data.remote.ApiHyphenation

@Entity(tableName = "wordnik_hyphenation")
data class HyphenationEntry(
  @PrimaryKey val id: String,
  val word: String,
  val hyphenations: List<Hyphenation>,
  val lastFetch: OffsetDateTime
) {
  companion object {
    fun fromRemote(word: String, apiHyphenation: List<ApiHyphenation>?): HyphenationEntry {
      return HyphenationEntry(
        word,
        word,
        apiHyphenation?.map {
          Hyphenation(
            it.text ?: "",
            it.seq ?: -1
          )
        } ?: emptyList(),
        OffsetDateTime.now()
      )
    }
  }
}

class Hyphenation(
  val text: String,
  val seq: Int
)