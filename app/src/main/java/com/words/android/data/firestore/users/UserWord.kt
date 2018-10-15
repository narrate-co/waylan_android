package com.words.android.data.firestore.users

import java.util.*

data class UserWord(
    var id: String = "",
    var word: String = "",
    var created: Date = Date(),
    var modified: Date = Date(),
    var types: MutableMap<String, Boolean> = mutableMapOf(),
    var partOfSpeechPreview: MutableMap<String, String> = mutableMapOf(),
    var defPreview: MutableMap<String, String> = mutableMapOf(),
    var synonymPreview: MutableMap<String, String> = mutableMapOf(),
    var labelsPreview: MutableMap<String, String> = mutableMapOf(),
    var totalViewCount: Long = 1
)

