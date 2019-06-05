package space.narrate.waylan.android

import space.narrate.waylan.android.data.firestore.DataOwners
import space.narrate.waylan.android.data.firestore.users.User
import space.narrate.waylan.android.data.firestore.users.UserWord
import space.narrate.waylan.android.data.firestore.users.UserWordType
import space.narrate.waylan.android.data.firestore.words.GlobalWord
import java.util.*

val testDatabase = FirestoreTestDatabase(
    FirestoreTestData.globalWords,
    listOf(
        FirestoreTestUserDocument(
            FirestoreTestData.user1.uid,
            FirestoreTestData.user1Words,
            FirestoreTestData.user1
        ),
        FirestoreTestUserDocument(
            FirestoreTestData.user2.uid,
            FirestoreTestData.user2Words,
            FirestoreTestData.user2
        )
    )
)

data class FirestoreTestDatabase(
    val globalWords: List<GlobalWord>,
    val users: List<FirestoreTestUserDocument>
)

data class FirestoreTestUserDocument(
    val uid: String,
    val userWords: List<UserWord>,
    val user: User
)

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

    val user1Words = listOf(
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


    val user2Words = listOf(
        UserWord(
            "345",
            "ostensibly",
            partOfSpeechPreview = mutableMapOf("adverb" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("in an ostensible manner" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("apparently" to DataOwners.WORDSET.name),
            types = mutableMapOf(UserWordType.FAVORITED.name to true)
        ),
        UserWord(
            "456",
            "impetuous",
            partOfSpeechPreview = mutableMapOf("adjective" to DataOwners.WORDSET.name),
            defPreview = mutableMapOf("characterized by undue haste and lack of thought or deliberation" to DataOwners.WORDSET.name),
            synonymPreview = mutableMapOf("tearaway" to DataOwners.WORDSET.name),
            types = mutableMapOf(UserWordType.RECENT.name to true)

        )
    )

    val user1 = User(
        "123",
        true,
        "User 1"
    )

    val user2 = User(
        "345",
        false,
        "User 2",
        "user2@gmail.com",
        Date(),
        "teset_merriam_webster_purchase_token"
    )
}
