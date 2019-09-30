package space.narrate.waylan.merriamwebster.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A Room convenience class to join a [MwWord] and all its child [MwDefinitionGroup]s with a single query
 */
data class MwWordAndDefinitionGroups(
    @Embedded
    var word: MwWord? = null,
    @Relation(parentColumn = "id", entityColumn = "parentId")
    var definitions: List<MwDefinitionGroup> = ArrayList()
)

