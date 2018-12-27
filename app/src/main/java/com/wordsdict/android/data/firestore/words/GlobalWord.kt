package com.wordsdict.android.data.firestore.words

import com.wordsdict.android.data.firestore.ViewCountDocument
import java.util.*

/**
 * A Firestore document that holds properties relevant to all user's (ie. viewCount and
 * possibly, in the future, user created public content such as additional examples and
 * definitions, like a wiki)
 *
 * @property id The Firestore document id of this object. This matches the [word] property
 * @property word The word as it appears in the dictionary
 * @property totalViewCount The total number of times this word has ever been viewed by all
 *  users.
 */
data class GlobalWord(
        var id: String = "",
        var word: String = "",
        var created: Date = Date(),
        var modified: Date = Date(),
        var partOfSpeechPreview: MutableMap<String, String> = mutableMapOf(),
        var defPreview: MutableMap<String, String> = mutableMapOf(),
        var synonymPreview: MutableMap<String, String> = mutableMapOf(),
        var labelsPreview: MutableMap<String, String> = mutableMapOf()
) : ViewCountDocument()