package space.narrate.waylan.android.data.mw

import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.android.data.disk.mw.MwWord

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