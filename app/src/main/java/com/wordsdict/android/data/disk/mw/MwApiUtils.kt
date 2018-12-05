package com.wordsdict.android.data.disk.mw

import com.wordsdict.android.data.mw.Entry
import com.wordsdict.android.data.mw.EntryList

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
            Sound(this.sounds.map { it.wav }.firstOrNull()?.firstOrNull()
                    ?: "", this.sounds.map { it.wpr }.firstOrNull()?.firstOrNull()
                    ?: ""), //TODO restructure db
            this.pronunciations.firstOrNull()?.value ?: "",
            this.partOfSpeech,
            this.etymology.value,
            relatedWords.filterNot { it == this.word },
            suggestions.filterNot { it == this.word },
            Uro(this.uro.firstOrNull()?.ure
                    ?: "", this.uro.firstOrNull()?.fl ?: ""))
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
                        orderedDefs
                )
        )
    }

fun toDbMwSuggestionWord(id: String, suggestions: List<String>): Word {
    return Word(
            id,
            id,
            "",
            "",
            Sound("", ""),
            "",
            "",
            "",
            emptyList(),
            suggestions,
            Uro("", "")
    )
}