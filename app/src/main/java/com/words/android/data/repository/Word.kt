package com.words.android.data.repository

import com.words.android.data.disk.Meaning
import com.words.android.data.disk.Word
import com.words.android.data.firestore.UserWord

data class Word(
        var dbWord: Word? = null,
        var fsWord: com.words.android.data.firestore.Word? = null,
        var userWord: UserWord? = null,
        var dbMeanings: List<Meaning> = emptyList(),
        var fsMeanings: List<com.words.android.data.firestore.Meaning>? = emptyList()
)