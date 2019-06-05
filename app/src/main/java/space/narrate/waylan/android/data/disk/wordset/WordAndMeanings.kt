package space.narrate.waylan.android.data.disk.wordset

import androidx.room.Embedded
import androidx.room.Relation
import space.narrate.waylan.android.data.disk.wordset.Meaning
import space.narrate.waylan.android.data.disk.wordset.Word

data class WordAndMeanings(
    @Embedded
    var word: Word? = null,
    @Relation(parentColumn = "word", entityColumn = "parentWord")
    var meanings: List<Meaning> = ArrayList()
)

