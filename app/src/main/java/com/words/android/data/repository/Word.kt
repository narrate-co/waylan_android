package com.words.android.data.repository

import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.data.disk.wordset.Meaning
import com.words.android.data.disk.wordset.Word
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.words.GlobalWord

data class Word(
        var dbWord: Word? = null,
        var userWord: UserWord? = null,
        var dbMeanings: List<Meaning> = emptyList(),
        var mwEntry: List<WordAndDefinitions> = emptyList(),
        var globalWord: GlobalWord? = null
)