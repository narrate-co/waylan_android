package space.narrate.waylan.merriamwebster_thesaurus.data.local

/**
 * The local representation of an entry retrieved from the Merriam-Webster thesaurus. This class
 * should be the only class used when displaying thesaurus data in the UI.
 */
data class ThesaurusEntry(
    val id: String,
    val word: String,
    val src: String,
    val stems: List<String>,
    val offensive: Boolean,
    val partOfSpeech: String,
    val shortDefs: List<String>,
    val synonymWords: List<String>,
    val relatedWords: List<String>,
    val nearWords: List<String>,
    val antonymWords: List<String>
)