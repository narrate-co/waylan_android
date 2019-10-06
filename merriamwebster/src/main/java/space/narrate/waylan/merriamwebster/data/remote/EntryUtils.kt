package space.narrate.waylan.merriamwebster.data.remote

import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.merriamwebster.data.local.MwWord

object EntryUtils {

    fun toDbMwSuggestionWord(id: String, suggestions: List<String>): MwWord {
        return MwWord(
            id,
            id,
            "",
            "",
            emptyList(),
            emptyList(),
            "",
            "",
            emptyList(),
            suggestions,
            emptyList(),
            OffsetDateTime.now()
        )
    }
}