package com.wordsdict.android.data.firestore.words

import java.util.*

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