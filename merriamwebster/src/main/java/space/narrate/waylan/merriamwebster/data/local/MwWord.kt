package space.narrate.waylan.merriamwebster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime


/**
 * A Merriam-Webster word
 *
 * @param id A unique identifier for each word. This is often the same as [word] except for
 *      when multiple entry exist for a word. For example, <i>quiet</i> might return
 *      3 entry from the Merriam-Webster API. In such a case the ids will often look like
 *      [quiet, quiet[1], quiet[2]]. When querying for a String, id should be ignored in favor
 *      of [word].
 * @param word The String value of the word as it appears in the dictionary
 * @param relatedWords A list of words (as they appear in the dictionary), which are slight
 *      variations of this [MwWord]. This is different from [suggestions] as [relatedWords] are
 *      returned for Merriam-Webster API requests that have non-empty responses
 * @param suggestions A list of words (as they appear in the dictionary), which are related to
 *      this [MwWord]. This field is usually only populated when a Merriam-Webster API request
 *      returns empty responses and instead return alternatives.
 */
@Entity(tableName = "mw_words")
data class MwWord(
    @PrimaryKey
    val id: String,
    val word: String,
    val subj: String,
    val phonetic: String,
    val sound: List<Sound>,
    val pronunciation: List<String>,
    val partOfSpeech: String,
    val etymology: String,
    val relatedWords: List<String>,
    var suggestions: List<String>,
    val uro: List<Uro>,
    val lastFetch: OffsetDateTime
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is MwWord) return false
        if (this === other) return true

        return id == other.id &&
            word == other.word &&
            subj == other.subj &&
            phonetic == other.phonetic &&
            partOfSpeech == other.partOfSpeech &&
            etymology == other.etymology
            // Uro not included
            // Sound not included
            // pronunciation not included
    }

    override fun toString(): String {
        return "$id, $word, $subj, $phonetic, $partOfSpeech, $etymology"
    }
}