package space.narrate.words.android.data.repository

import space.narrate.words.android.data.mw.PermissiveWordsDefinitions
import space.narrate.words.android.data.disk.wordset.Word
import space.narrate.words.android.data.disk.wordset.WordAndMeanings
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.words.GlobalWord
import space.narrate.words.android.data.spell.SuggestItem

/**
 * A sealed class used to enumerate the different sources from which a word, and its related
 * data, can come from.
 *
 * With so many different data stores, it becomes necessary to have a higher level abstraction
 * to understand where a piece of data has come from. WordSource serves that purpose.
 */
sealed class WordSource

/**
 * A simple copy of a word which has been queried. This WordSource's primary purpose is to
 * immediately essentially copy a query and be immediately available in order to provide
 * initial and crucial UI data.
 */
class WordPropertiesSource(val id: String, val word: String): WordSource()

/**
 * A wrapper for a Wordset [Word]. This WordSource's primary purpose is to hold data for
 * WordSet words where a word's definitions aren't needed.
 */
class SimpleWordSource(val word: Word): WordSource()

/**
 * A wrapper for a [SuggestItem] returned from SymSpell. This WordSource's primary purpose is to
 * represent a suggested spelling for a given input String.
 */
class SuggestSource(val item: SuggestItem): WordSource()

/**
 * A wrapper for a WordSet [Word] and it's [Meaning]s.
 */
class WordsetSource(val wordAndMeaning: WordAndMeanings): WordSource()

/**
 * A wrapper for a [UserWord] which has been saved to a user's /users/{userId}/words collection by
 * containing any listType of [UserWordType].
 */
class FirestoreUserSource(val userWord: UserWord): WordSource()

/**
 * A wrapper for a [GlobalWord] which has been viewed by <i>any</i> user
 */
class FirestoreGlobalSource(val globalWord: GlobalWord): WordSource()

/**
 * A wrapper for both a Merriam-Webster MwWord and all it's child Definitions and the current User.
 * The included User object is useful for determining permissions when setting and displaying
 * UI elements.
 */
class MerriamWebsterSource(val wordsDefinitions: PermissiveWordsDefinitions): WordSource()
