package space.narrate.words.android.ui.details

import space.narrate.words.android.data.repository.*
import space.narrate.words.android.data.repository.*
import kotlin.reflect.KClass

/**
 * A class that handles holding all submitted [WordSource]s, determining their validity, and
 * generating a list of [DetailsComponent]s, ordered in the way they should be rendered in the
 * adapter, to be submitted to [DetailsAdapter].
 *
 * Note: Not all [WordSource]s are used by [WordSourceList] (ex. [SimpleWordSource] is not used
 * since it does not provide any relevant details and therefore does not have a
 * corresponding [DetailsComponent]). Similarly, some [WordSource]s are broken up into more
 * than one [DetailsComponent] to help separate logical UI groups (ex. [WordsetSource] is split
 * up into both [DetailsComponent.WordsetComponent] to hold definitions and synonyms and
 * [DetailsComponent.ExamplesComponent] to hold wordset examples. This is because those two
 * components are likely to be updated (and animated) independently.
 */
class WordSourceList {

    private var wordId: String = ""

    private var propertiesSource: WordPropertiesSource? = null
    private var wordset: WordsetSource? = null
    private var merriamWebster: MerriamWebsterSource? = null
    private var firestoreUser: FirestoreUserSource? = null
    private var firestoreGlobal: FirestoreGlobalSource? = null

    /**
     * Add a source to the holder. If the source is relevant (it is valid and should be
     * included), the sources backing property is set and the source's corresponding
     * [DetailsComponent] will be included in the returned list from [getComponentsList]
     *
     * @return true if the call has changed something which will alter the returned value
     *  of [getComponentsList], indicating a new [DetailsComponent] list should be submitted
     *  to [DetailsAdapter]
     */
    fun add(source: WordSource): Boolean {

        // We should always return true if the word (as it appears in the dictionary) has
        // changed
        val hasCleared = clearIfNewWordSource(source)

        val hasChanged = when (source) {
            is WordPropertiesSource -> {
                propertiesSource = source
                true
            }
            is WordsetSource -> {
                wordset = source
                true
            }
            is MerriamWebsterSource -> {
                if (merriamWebster == null // the source is null and should be cleared
                        || source.wordsDefinitions.entries // the entries contain definitions
                                .map { it.definitions }
                                .flatten()
                                .isNotEmpty()
                        || source.wordsDefinitions.entries // the entries contain suggestions
                                .map { it.word }
                                .filterNotNull()
                                .map { it.suggestions }
                                .flatten()
                                .isNotEmpty()
                ) {
                    merriamWebster = source
                    true
                } else {
                    false
                }
            }
            is FirestoreUserSource -> {
                firestoreUser = source
                true
            }
            is FirestoreGlobalSource -> {
                firestoreGlobal = source
                true
            }
            else -> false
        }

        return hasCleared || hasChanged
    }

    /**
     * Remove the [WordSource] [type] from the list. The next time [getComponentsList] is
     * called, the corresponding [DetailsComponent] for [type] will not be present
     *
     * *
     * @return true if the call has changed something which will alter the returned value
     *  of [getComponentsList], indicating a new [DetailsComponent] list should be submitted
     *  to [DetailsAdapter]
     */
    fun remove(type: KClass<out WordSource>): Boolean {
        return when (type) {
            WordPropertiesSource::class -> {
                if (propertiesSource != null) {
                    propertiesSource = null
                    true
                } else {
                    false
                }
            }
            WordsetSource::class -> {
                if (wordset != null) {
                    wordset = null
                    true
                } else {
                    false
                }
            }
            MerriamWebsterSource::class -> {
                if (merriamWebster != null) {
                    merriamWebster = null
                    true
                } else {
                    false
                }
            }
            FirestoreUserSource::class -> {
                if (firestoreUser != null) {
                    firestoreUser = null
                    true
                } else {
                    false
                }
            }
            FirestoreGlobalSource::class -> {
                if (firestoreGlobal != null) {
                    firestoreGlobal = null
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    /**
     * A function to construct a list of [DetailsComponent] based on the available [WordSource]
     * backing properties.
     *
     * @return a List of [DetailsComponent] in the order in which they should be displayed by
     *  the adapter
     */
    fun getComponentsList(): List<DetailsComponent> {
        val list = mutableListOf<DetailsComponent>()
        // add title component if properties source if not null
        propertiesSource?.let { list.add(DetailsComponent.TitleComponent(it)) }
        // add merriam-webster component if merriam-webster source not null and entries is not empty
        merriamWebster?.let {
            if (it.wordsDefinitions.entries.isNotEmpty()) {
                list.add(DetailsComponent.MerriamWebsterComponent(it))
            }
        }
        // add wordset component if wordset source is not null
        wordset?.let { list.add(DetailsComponent.WordsetComponent(it)) }

        // add examples component if there are any examples in wordset source
        // TODO change this to always show if allowing users to add their own
        val examples = (wordset?.wordAndMeaning?.meanings?.map { it.examples } ?: emptyList()).flatten()
        if (examples.isNotEmpty()) {
            wordset?.let { list.add(DetailsComponent.ExamplesComponent(it)) }
        }
        return list
    }

    /**
     * If the source being submitted is a different word (as it appears in the dictionary)
     * than what's currently submitted, clear the entire list.
     *
     * @return true if the list has been cleared due to a new word being submitted
     */
    private fun clearIfNewWordSource(source: WordSource): Boolean {
        val newWordId: String? = when (source) {
            is WordsetSource -> source.wordAndMeaning.word?.word
            is MerriamWebsterSource -> source.wordsDefinitions?.entries.firstOrNull()?.word?.word
            is FirestoreUserSource -> source.userWord.word
            is FirestoreGlobalSource -> source.globalWord.word
            else -> return false
        }


        if (newWordId != null && newWordId.isNotBlank() && newWordId != wordId) {
            //new word coming in
            // clear source holder
            wordset = null
            merriamWebster = null
            firestoreUser = null
            firestoreGlobal = null

            wordId = newWordId
            return true
        } else {
            //same word. do nothing
            return false
        }
    }
}