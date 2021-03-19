package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.data.remote.ApiFrequency

@Entity(tableName = "wordnik_frequency")
data class FrequencyEntry(
  @PrimaryKey val id: String,
  val word: String,
  val totalCount: Int,
  val unknownYearCount: Int,
  val frequencies: List<Frequency>,
  val lastFetch: OffsetDateTime
) {
  companion object {
    fun fromRemote(word: String, apiFrequency: ApiFrequency?): FrequencyEntry {
      return FrequencyEntry(
        word,
        word,
        apiFrequency?.totalCount ?: 0,
        apiFrequency?.unknownYearCount ?: 0,
        apiFrequency?.frequency?.map {
          Frequency(
            it.year ?: "",
            it.count ?: 0
          )
        } ?: emptyList(),
        OffsetDateTime.now()
      )
    }
  }
}

class Frequency(
  val year: String,
  val count: Int
)