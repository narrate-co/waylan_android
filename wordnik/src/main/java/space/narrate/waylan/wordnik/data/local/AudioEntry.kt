package space.narrate.waylan.wordnik.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.data.remote.ApiAudio

@Entity(tableName = "wordnik_audio")
data class AudioEntry(
  @PrimaryKey val id: String,
  val word: String,
  val audios: List<Audio>,
  val lastFetch: OffsetDateTime
) {
  companion object {
    fun fromRemote(word: String, apiAudio: List<ApiAudio>?): AudioEntry {
      return AudioEntry(
        word,
        word,
        apiAudio?.map {
          Audio(
            it.id ?: 0,
            it.word ?: word,
            it.commentCount ?: 0,
            it.createdBy ?: "",
            it.createdAt ?: "",
            it.duration ?: 0.0F,
            it.audioType ?: "",
            it.attributionText ?: "",
            it.attributionUrl ?: "",
            it.fileUrl ?: ""
          )
        } ?: emptyList(),
        OffsetDateTime.now()
      )
    }
  }
}

class Audio(
  val id: Int,
  val word: String,
  val commentCount: Int,
  val createdBy: String,
  val createdAt: String,
  val duration: Float,
  val audioType: String,
  val attributionText: String,
  val attributionUrl: String,
  val fileUrl: String,
)