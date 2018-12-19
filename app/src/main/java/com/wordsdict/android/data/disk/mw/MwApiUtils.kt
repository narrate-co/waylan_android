package com.wordsdict.android.data.disk.mw

import com.wordsdict.android.data.mw.Entry
import com.wordsdict.android.data.mw.EntryList
import org.threeten.bp.OffsetDateTime
import java.util.*

/**
 * Utilities to help convert [EntryList]s and [Entry]s to [Word], [Definition] and all
 * related, local, Room objects.
 */

val EntryList.synthesizedRelatedWords: List<String>
    get() = this.entries.map { it.word }.distinct()


val EntryList.synthesizedSuggestions: List<String>
    get() = (this.entries.map { it.word } + this.suggestions).distinct()

fun Entry.toDbMwWord(relatedWords: List<String>, suggestions: List<String>): Word {
    return Word(
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

val Entry.toDbMwDefinitions: List<Definition>
    get() {
        val orderedDefs = this.def.dts.mapIndexed { index, formattedString ->
            val sn = this.def.sn.getOrNull(index) ?: (index + 1).toString()
            OrderedDefinitionItem(sn, formattedString.value)
        }
        return listOf(
                Definition(
                        "${this.id}${orderedDefs.hashCode()}",
                        this.id,
                        this.word,
                        this.def.date,
                        orderedDefs,
                        OffsetDateTime.now()
                )
        )
    }

fun toDbMwSuggestionWord(id: String, suggestions: List<String>): Word {
    return Word(
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