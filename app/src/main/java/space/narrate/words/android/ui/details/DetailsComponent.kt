package space.narrate.words.android.ui.details

import space.narrate.words.android.data.repository.MerriamWebsterSource
import space.narrate.words.android.data.repository.WordPropertiesSource
import space.narrate.words.android.data.repository.WordSource
import space.narrate.words.android.data.repository.WordsetSource

sealed class DetailsComponent(
        val source: WordSource,
        val type: Int
) : Diffable<DetailsComponent> {

    companion object {
        /**
         * A list of Ints  to be used by [DetailsAdapter.getItemViewType]
         */
        const val VIEW_TYPE_TITLE = 1
        const val VIEW_TYPE_MERRIAM_WEBSTER = 2
        const val VIEW_TYPE_WORDSET = 3
        const val VIEW_TYPE_EXAMPLE = 4
    }

    /**
     * A component to represent the Title item layout that simply contains the word (as it appears
     * in the dictionary)
     */
    class TitleComponent(source: WordPropertiesSource): DetailsComponent(source, VIEW_TYPE_TITLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            // Are items of the same type
            return newOther is TitleComponent
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            // do items point to the same address
            if (this == newOther) return true
            if (source !is WordPropertiesSource || newOther.source !is WordPropertiesSource) {
                return false
            }

            // TitleComponents should only be considered changed if their word (as it appears in
            // the dictionary) has changed.
            return source.word == newOther.source.word
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    /**
     * A component to represent the MerriamWebsterCardView item layout that handles the display of all
     * Merriam-Webster data abut a word.
     */
    class MerriamWebsterComponent(
            source: MerriamWebsterSource
    ) : DetailsComponent(source, VIEW_TYPE_MERRIAM_WEBSTER) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            // Are items of the same type
            return newOther is MerriamWebsterComponent
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            // do items point to the same address
            if (this == newOther) return true
            if (source !is MerriamWebsterSource || newOther.source !is MerriamWebsterSource) {
                return false
            }

            val wordsSize = newOther.source
                    .wordsDefinitions
                    .entries
                    .map { it.word?.word }
                    .filterNotNull()
                    .size

            val defsSize = newOther.source
                    .wordsDefinitions
                    .entries
                    .map { it.definitions }
                    .flatten()
                    .map { it.id }
                    .size

            // Since this component just shows definitions, don't call for updates if there aren't
            // any definitions
            if (wordsSize != 0 && defsSize == 0) {
                return true
            }

            // Contents should be considered equal if all words and definitions present in the new
            // component are already present in the old component (nothing has been added,
            // changed or removed)
            return source.wordsDefinitions
                    .entries
                    .toTypedArray()
                    .contentDeepEquals(newOther.source.wordsDefinitions.entries.toTypedArray())
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    /**
     * A component to represent the Wordset definitions item layout that handles the display of
     * all of a word's WordSet definitions.
     */
    class WordsetComponent(source: WordsetSource): DetailsComponent(source, VIEW_TYPE_WORDSET) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            // Are items of the same type
            return newOther is WordsetComponent
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            // do items point to the same address
            if (this == newOther) return true
            if (source !is WordsetSource || newOther.source !is WordsetSource) return false

            // Contents should be considered equal if all the definitions in the new component
            // are also present in the old component

            return source.wordAndMeaning.meanings
                    .map { it.def }
                    .toTypedArray()
                    .contentDeepEquals(
                            newOther.source.wordAndMeaning.meanings
                                    .map { it.def }
                                    .toTypedArray()
                    )
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }

    /**
     * A component to represent the WordSet example item layout that handles the display of
     * all of a word's WordSet examples.
     *
     * Note: This will likely represent the user added examples in the future also
     */
    class ExamplesComponent(source: WordsetSource): DetailsComponent(source, VIEW_TYPE_EXAMPLE) {
        override fun equalTo(newOther: DetailsComponent): Boolean {
            // Are items of the same type
            return newOther is ExamplesComponent
        }

        override fun contentsSameAs(newOther: DetailsComponent): Boolean {
            // do items point to the same address
            if (this == newOther) return true
            if (source !is WordsetSource || newOther.source !is WordsetSource) return false

            // Contents should be considered equal if all examples present in the new component
            // are also present in the old component
            return source.wordAndMeaning.meanings
                    .map { it.examples }
                    .toTypedArray()
                    .contentDeepEquals(
                            newOther.source.wordAndMeaning.meanings
                                    .map { it.examples }
                                    .toTypedArray()
                    )
        }

        override fun getChangePayload(newOther: DetailsComponent): Any? {
            return null
        }
    }
}

