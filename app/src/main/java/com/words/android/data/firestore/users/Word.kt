package com.words.android.data.firestore.users

import java.util.*

data class Word(
        var id: String = "",
        var contributorIds: MutableMap<String, Boolean> = mutableMapOf(),
        var word: String = "",
        var created: Date = Date(),
        var modified: Date = Date(),
        var favoriteCount: Int = 0

        //Nested Meanings Collection
)

