package com.words.android.ui.details

import com.words.android.data.repository.Word
import com.words.android.util.Diffable
import java.lang.RuntimeException

sealed class DetailsComponent(val word: Word, val type: Type): Diffable<DetailsComponent> {

    enum class Type(val number: Int) {
        TITLE(1), MERRIAM_WEBSTER(2), WORDSET(3), EXAMPLE(4)
    }

    class TitleComponent(word: Word): DetailsComponent(word, Type.TITLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is TitleComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {

            return false
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class MerriamWebsterComponent(word: Word): DetailsComponent(word, Type.MERRIAM_WEBSTER) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is MerriamWebsterComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            //TODO make full checks
            val sameWords = word.mwEntry.map { it.word }.toList().containsAll(newOther.word.mwEntry.map { it.word }.toList())
            val sameDefinitions = word.mwEntry.map { it.definitions }.flatten().toList().containsAll(newOther.word.mwEntry.map { it.definitions }.flatten().toList())
            return sameWords && sameDefinitions
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class WordsetComponent(word: Word): DetailsComponent(word, Type.WORDSET) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is WordsetComponent) return false
            //TODO make newOther comparisons

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            //TODO make full checks

            return false
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class ExamplesComponent(word: Word): DetailsComponent(word,Type.EXAMPLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is ExamplesComponent) return false
            //TODO make newOther comparisons

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            //TODO make full checks
            return false
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }
}

