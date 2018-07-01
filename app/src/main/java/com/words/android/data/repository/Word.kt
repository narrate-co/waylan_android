package com.words.android.data.repository

import com.words.android.data.disk.mw.Definition
import com.words.android.data.disk.wordset.Meaning
import com.words.android.data.disk.wordset.Word
import com.words.android.data.firestore.UserWord

data class Word(
        var dbWord: Word? = null,
        var fsWord: com.words.android.data.firestore.Word? = null,
        var userWord: UserWord? = null,
        var dbMeanings: List<Meaning> = emptyList(),
        var fsMeanings: List<com.words.android.data.firestore.Meaning>? = emptyList(),
        var mwWord: com.words.android.data.disk.mw.Word? = null,
        var mwDefinitions: List<Definition> = emptyList()
)