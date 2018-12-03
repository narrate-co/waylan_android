package com.wordsdict.android.data.firestore.words

import java.util.*

/**
 * A Firestore document that holds properties relevant to all user's (ie. viewCount and
 * possibly, in the future, user created public content such as additional examples and definitions, like a wiki)
 */
data class GlobalWord(
        var id: String = "",
        var word: String = "",
        var created: Date = Date(),
        var modified: Date = Date(),
        var partOfSpeechPreview: MutableMap<String, String> = mutableMapOf(),
        var defPreview: MutableMap<String, String> = mutableMapOf(),
        var synonymPreview: MutableMap<String, String> = mutableMapOf(),
        var labelsPreview: MutableMap<String, String> = mutableMapOf(),
        var totalViewCount: Long = 0L
)