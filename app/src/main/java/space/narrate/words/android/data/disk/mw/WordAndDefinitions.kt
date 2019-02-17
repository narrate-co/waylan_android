package space.narrate.words.android.data.disk.mw

import androidx.room.Embedded
import androidx.room.Relation
import space.narrate.words.android.data.disk.mw.Word

/**
 * A Room convenience class to join a [Word] and all its child [Definition]s with a single query
 */
data class WordAndDefinitions(
        @Embedded
        var word: Word? = null,
        @Relation(parentColumn = "id", entityColumn = "parentId")
        var definitions: List<Definition> = ArrayList()
)

