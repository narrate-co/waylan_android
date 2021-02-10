package space.narrate.waylan.test_common

import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOnActionUseCase
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.users.UserWordType
import space.narrate.waylan.core.data.firestore.words.GlobalWord
import space.narrate.waylan.core.util.minusDays
import java.util.*
import space.narrate.waylan.core.data.firestore.DataOwners

data class FirestoreTestDatabase(
    val globalWords: List<GlobalWord>,
    val users: List<FirestoreTestUserDocument>
)

data class FirestoreTestUserDocument(
    val uid: String,
    val user: User,
    val userWords: List<UserWord>,
    val addOns: List<UserAddOn>
)

object FirestoreTestData {
    private val globalWords = listOf(
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

    private val user1 = User(
        "123",
        true,
        "User 1"
    )

    private val user2 = User(
        "345",
        false,
        "User 2",
        "user2@gmail.com",
        Date(),
        "teset_merriam_webster_purchase_token"
    )

    private val user1Words = listOf(
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

    private val user1AddOns = listOf(
        // AddOnState.FREE_TRIAL_VALID
        UserAddOnActionUseCase.TryForFree.perform(user1, UserAddOn(AddOn.MERRIAM_WEBSTER.id)),
        // AddOnState.NONE
        UserAddOn(AddOn.MERRIAM_WEBSTER_THESAURUS.id)
    )


    private val user2Words = listOf(
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

    private val user2AddOns = listOf(
        // AddOnState.PURCHASED_VALID
        UserAddOnActionUseCase.Add("test_purchase_tocken").perform(user2, UserAddOn(AddOn.MERRIAM_WEBSTER.id)),
        // AddOnState.FREE_TRIAL_EXPIRED
        UserAddOnActionUseCase.TryForFree.perform(user2, UserAddOn(AddOn.MERRIAM_WEBSTER_THESAURUS.id)).apply {
            started = started.minusDays(validDurationDays + 1L)
        }
    )

    val testDatabase = FirestoreTestDatabase(
        globalWords,
        listOf(
            FirestoreTestUserDocument(
                user1.uid,
                user1,
                user1Words,
                user1AddOns
            ),
            FirestoreTestUserDocument(
                user2.uid,
                user2,
                user2Words,
                user2AddOns
            )
        )
    )
}
