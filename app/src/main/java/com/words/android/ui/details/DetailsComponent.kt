package com.words.android.ui.details

import com.words.android.data.repository.WordSource
import com.words.android.util.Diffable

sealed class DetailsComponent(val source: WordSource, val type: Type): Diffable<DetailsComponent> {

    enum class Type(val number: Int) {
        TITLE(1), MERRIAM_WEBSTER(2), WORDSET(3), EXAMPLE(4)
    }

    class TitleComponent(source: WordSource.WordPropertiesSource): DetailsComponent(source, Type.TITLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is TitleComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordPropertiesSource || newOther.source !is WordSource.WordPropertiesSource) return false

            return source.props.word == newOther.source.props.word
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class MerriamWebsterComponent(source: WordSource.MerriamWebsterSource): DetailsComponent(source, Type.MERRIAM_WEBSTER) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is MerriamWebsterComponent) return false
            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.MerriamWebsterSource || newOther.source !is WordSource.MerriamWebsterSource) return false

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

    class WordsetComponent(source: WordSource.WordsetSource): DetailsComponent(source, Type.WORDSET) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is WordsetComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordsetSource || newOther.source !is WordSource.WordsetSource) return false
            //TODO make full checks

            val isTheSame = source.wordAndMeaning.meanings.map { it.def }.toTypedArray().contentDeepEquals(newOther.source.wordAndMeaning.meanings.map { it.def }.toTypedArray())


            return isTheSame
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class ExamplesComponent(source: WordSource.WordsetSource): DetailsComponent(source,Type.EXAMPLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is ExamplesComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordsetSource || newOther.source !is WordSource.WordsetSource) return false
            //TODO make full checks

            val isTheSame = source.wordAndMeaning.meanings.map { it.examples }.toTypedArray().contentDeepEquals(newOther.source.wordAndMeaning.meanings.map { it.examples }.toTypedArray())


            return isTheSame
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }
}

