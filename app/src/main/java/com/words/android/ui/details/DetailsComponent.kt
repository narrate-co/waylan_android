package com.words.android.ui.details

import com.words.android.data.repository.WordSource
import com.words.android.util.Diffable

sealed class DetailsComponent(val source: WordSource, val type: Type): Diffable<DetailsComponent> {

    enum class Type(val number: Int) {
        TITLE(1), MERRIAM_WEBSTER(2), WORDSET(3), EXAMPLE(4)
    }

    class TitleComponent(source: WordSource.WordProperties): DetailsComponent(source, Type.TITLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is TitleComponent) return false

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordProperties || newOther.source !is WordSource.WordProperties) return false

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

            val wordsSize = newOther.source.wordsAndDefs.map { it.word?.word }.filterNotNull().size
            val defsSize = newOther.source.wordsAndDefs.map { it.definitions }.flatten().map { it.id }.size

            if (wordsSize != 0 && defsSize == 0) {
                println("DetailsComponent::MW wordsSize: $wordsSize, defsSize: $defsSize")
                return true
            }

            val sameWords = source.wordsAndDefs.map { it.word?.word }.filterNotNull().toList().containsAll(newOther.source.wordsAndDefs.map { it.word?.word }.filterNotNull().toList())
            val sameDefinitions = source.wordsAndDefs.map { it.definitions }.flatten().map { it.id }.toList().containsAll(newOther.source.wordsAndDefs.map { it.definitions }.flatten().map { it.id }.toList())

            return sameWords && sameDefinitions
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class WordsetComponent(source: WordSource.WordsetSource): DetailsComponent(source, Type.WORDSET) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is WordsetComponent) return false
            //TODO make newOther comparisons

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordsetSource || newOther.source !is WordSource.WordsetSource) return false
            //TODO make full checks

            return false
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    class ExamplesComponent(source: WordSource.WordsetSource): DetailsComponent(source,Type.EXAMPLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            if (newOther !is ExamplesComponent) return false
            //TODO make newOther comparisons

            return true
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            if (this == newOther) return true
            if (source !is WordSource.WordsetSource || newOther.source !is WordSource.WordsetSource) return false
            //TODO make full checks
            return false
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }
}

