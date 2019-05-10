package space.narrate.words.android.data.firestore.users

import space.narrate.words.android.data.firestore.ViewCountDocument
import java.util.*

/**
 * A Firestore document to hold mutable data about [User] related words. This includes things
 * like whether a word has been recently viewed or favorited.
 *
 * @property id the Firestore document id. This is the same as [word]
 * @property word The word this document represents (as it appears in the dictionary)
 * @property modified Updated each time any field has changed, including values in
 *  [UserWord.types]. This can be used to determine do things like sort by <i>most</i> recently
 *  viewed
 * @property types A mapTransform that flags this word as a [UserWordType]. This is useful since Firestore
 *  documents can be queried against values in a mapTransform property.
 * @property defPreview A short list of definitions which belong to this dictionary word. This is
 *  useful for querying for [UserWord] lists and immediately showing them with definitions
 *  without the need to make a subsequent query or join.
 *  @property totalViewCount The total number of times the user has viewed this word
 */
data class UserWord(
    var id: String = "",
    var word: String = "",
    var created: Date = Date(),
    var modified: Date = Date(),
    var types: MutableMap<String, Boolean> = mutableMapOf(),
    var partOfSpeechPreview: MutableMap<String, String> = mutableMapOf(),
    var defPreview: MutableMap<String, String> = mutableMapOf(),
    var synonymPreview: MutableMap<String, String> = mutableMapOf(),
    var labelsPreview: MutableMap<String, String> = mutableMapOf()
) : ViewCountDocument()



