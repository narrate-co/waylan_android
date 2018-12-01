package com.wordsdict.android.data.repository

import com.wordsdict.android.data.disk.mw.PermissiveWordsDefinitions
import com.wordsdict.android.data.disk.wordset.Word
import com.wordsdict.android.data.disk.wordset.WordAndMeanings
import com.wordsdict.android.data.firestore.users.UserWord
import com.wordsdict.android.data.firestore.words.GlobalWord
import com.wordsdict.android.data.spell.SuggestItem

sealed class WordSource

//success responses
class WordPropertiesSource(val props: com.wordsdict.android.data.repository.WordProperties): WordSource()
class SimpleWordSource(val word: Word): WordSource()
class SuggestSource(val item: SuggestItem): WordSource()
class WordsetSource(val wordAndMeaning: WordAndMeanings): WordSource()
class FirestoreUserSource(val userWord: UserWord): WordSource()
class FirestoreGlobalSource(val globalWord: GlobalWord): WordSource()
class MerriamWebsterSource(val wordsDefinitions: PermissiveWordsDefinitions): WordSource()
