package space.narrate.waylan.merriamwebster_thesaurus.data.local

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