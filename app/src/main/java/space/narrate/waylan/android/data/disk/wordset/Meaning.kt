package space.narrate.waylan.android.data.disk.wordset

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meanings",
    indices = [(Index("parentWord"))],
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["word"],
            childColumns = ["parentWord"],
            deferred = true)
    ]
)
data class Meaning(
    val parentWord: String,
    val def: String,
    val examples: List<Example>,
    val partOfSpeech: String,
    val synonyms: List<Synonym>,
    val labels: List<Label>,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
