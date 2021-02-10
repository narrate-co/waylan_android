package space.narrate.waylan.core.data.wordset

import org.threeten.bp.OffsetDateTime

data class Synonym(
    val synonym: String,
    val created: OffsetDateTime,
    val modified: OffsetDateTime
)

