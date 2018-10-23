package com.words.android.data.repository

import com.words.android.data.disk.mw.PermissiveWordsDefinitions
import com.words.android.data.disk.mw.WordAndDefinitions
import com.words.android.data.disk.wordset.Word
import com.words.android.data.disk.wordset.WordAndMeanings
import com.words.android.data.firestore.users.UserWord
import com.words.android.data.firestore.words.GlobalWord

sealed class WordSource {
    class WordProperties(val props: com.words.android.data.repository.WordProperties): WordSource()
    class SimpleWordSource(val word: Word): WordSource()
    class WordsetSource(val wordAndMeaning: WordAndMeanings): WordSource()
    class FirestoreUserSource(val userWord: UserWord): WordSource()
    class FirestoreGlobalSource(val globalWord: GlobalWord): WordSource()
    class MerriamWebsterSource(val wordsDefinitions: PermissiveWordsDefinitions): WordSource()
}

