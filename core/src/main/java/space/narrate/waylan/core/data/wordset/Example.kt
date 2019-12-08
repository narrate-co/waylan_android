package space.narrate.waylan.core.data.wordset

import org.threeten.bp.OffsetDateTime

data class Example(
    val example: String,
    val created: OffsetDateTime,
    val modified: OffsetDateTime
)

