package space.narrate.words.android.ui.auth

import space.narrate.words.android.data.disk.wordset.Label
import space.narrate.words.android.data.firestore.DataOwners
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.users.UserWordType
import space.narrate.words.android.data.firestore.words.GlobalWord
import java.util.*

object FirestoreTestData {
    val globalWords = listOf(
        GlobalWord(
            "123",
            "wharf",
            partOfSpeechPreview = mutableMapOf("noun" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("the bank of a river or the shore of the sea" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("dock" to DataOwners.WORDSET.name)
        ),
        GlobalWord(
            "234",
            "mercurial",
            partOfSpeechPreview = mutableMapOf("adjective" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("characterized by rapid and unpredictable changeableness of mood" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("erratic" to DataOwners.WORDSET.name)
        )
    )

    val userWords = listOf(
        UserWord(
            "345",
            "ostensibly",
            partOfSpeechPreview = mutableMapOf("adverb" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("in an ostensible manner" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("apparently" to DataOwners.WORDSET.name),
                types = mutableMapOf(UserWordType.RECENT.name to true)
        ),
        UserWord(
            "456",
            "impetuous",
            partOfSpeechPreview = mutableMapOf("adjective" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("characterized by undue haste and lack of thought or deliberation" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("tearaway" to DataOwners.WORDSET.name),
            types = mutableMapOf(UserWordType.FAVORITED.name to true)

        )
    )
}