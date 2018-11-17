package com.wordsdict.android.ui.details

import com.wordsdict.android.data.repository.MerriamWebsterSource
import com.wordsdict.android.data.repository.WordPropertiesSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.data.repository.WordsetSource
import com.wordsdict.android.util.Diffable

sealed class DetailsComponent(val source: WordSource, val type: Type): Diffable<DetailsComponent> {

    enum class Type(val number: Int) {
        TITLE(1), MERRIAM_WEBSTER(2), WORDSET(3), EXAMPLE(4)
    }

    class TitleComponent(source: WordPropertiesSource): DetailsComponent(source, Type.TITLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is TitleComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordPropertiesSource || newOther.source !is WordPropertiesSource) return false

            return source.props.word == newOther.source.props.word
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class MerriamWebsterComponent(source: MerriamWebsterSource): DetailsComponent(source, Type.MERRIAM_WEBSTER) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is MerriamWebsterComponent) return false
            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is MerriamWebsterSource || newOther.source !is MerriamWebsterSource) return false

            val wordsSize = newOther.source.wordsDefinitions.entries.map { it.word?.word }.filterNotNull().size
            val defsSize = newOther.source.wordsDefinitions.entries.map { it.definitions }.flatten().map { it.id }.size

            if (wordsSize != 0 && defsSize == 0) {
                return true
            }

            val sameDefinitions = source.wordsDefinitions.entries.toTypedArray().contentDeepEquals(newOther.source.wordsDefinitions.entries.toTypedArray())


            return sameDefinitions
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class WordsetComponent(source: WordsetSource): DetailsComponent(source, Type.WORDSET) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is WordsetComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordsetSource || newOther.source !is WordsetSource) return false
            //TODO make full checks

            val isTheSame = source.wordAndMeaning.meanings.map { it.def }.toTypedArray().contentDeepEquals(newOther.source.wordAndMeaning.meanings.map { it.def }.toTypedArray())


            return isTheSame
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class ExamplesComponent(source: WordsetSource): DetailsComponent(source,Type.EXAMPLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is ExamplesComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordsetSource || newOther.source !is WordsetSource) return false
            //TODO make full checks

            val isTheSame = source.wordAndMeaning.meanings.map { it.examples }.toTypedArray().contentDeepEquals(newOther.source.wordAndMeaning.meanings.map { it.examples }.toTypedArray())


            return isTheSame
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }
}

