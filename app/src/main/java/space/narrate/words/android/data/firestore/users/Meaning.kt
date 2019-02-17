package space.narrate.words.android.data.firestore.users

import java.util.*

data class Meaning(
        var id :String = "",
        var parentWord: String = "",
        var owner: String = "",
        var def: String = "",
        var examples: MutableMap<String, String> = mutableMapOf(),
        var partOfSpeech: String = "",
        var synonyms: MutableMap<String, String> = mutableMapOf(),
        var labels: MutableMap<String, String> = mutableMapOf(),
        var created: Date = Date(),
        var modified: Date = Date()
)

