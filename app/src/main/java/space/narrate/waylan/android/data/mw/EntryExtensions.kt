package space.narrate.waylan.android.data.mw

import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.android.data.disk.mw.MwDefinition
import space.narrate.waylan.android.data.disk.mw.MwDefinitionGroup
import space.narrate.waylan.android.data.disk.mw.MwWord
import space.narrate.waylan.android.data.disk.mw.Sound
import space.narrate.waylan.android.data.disk.mw.Uro

/**
 * Utilities to help convert [EntryList]s and [Entry]s to [MwWord], [MwDefinitionGroup] and all
 * related, local, Room objects.
 */

val EntryList.synthesizedRelatedWords: List<String>
    get() = this.entries.map { it.word }.distinct()


val EntryList.synthesizedSuggestions: List<String>
    get() = (this.entries.map { it.word } + this.suggestions).distinct()

fun Entry.toDbMwWord(relatedWords: List<String>, suggestions: List<String>): MwWord {
    return MwWord(
        this.id,
        this.word,
        this.subj,
        this.phonetic,
        this.sounds.map { Sound(it.wav, it.wpr) },
        this.pronunciations.map { it.toString() },
        this.partOfSpeech,
        this.etymology.value,
        relatedWords.filterNot { it == this.word },
        suggestions.filterNot { it == this.word },
        uro.map { Uro(it.ure, it.fl) },
        OffsetDateTime.now()
    )
}

val Entry.toDbMwDefinitions: List<MwDefinitionGroup>
    get() {
        val orderedDefs = this.def.dts.mapIndexed { index, formattedString ->
            val sn = this.def.sn.getOrNull(index) ?: (index + 1).toString()
            MwDefinition(sn, formattedString.value)
        }
        return listOf(
            MwDefinitionGroup(
                "${this.id}${orderedDefs.hashCode()}",
                this.id,
                this.word,
                this.def.date,
                orderedDefs,
                OffsetDateTime.now()
            )
        )
    }
