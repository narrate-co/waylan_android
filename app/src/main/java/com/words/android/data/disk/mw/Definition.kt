package com.words.android.data.disk.mw

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "mw_definitions",
        indices = [(Index("parentId", "parentWord"))],
        foreignKeys = [
                ForeignKey(
                        entity = Word::class,
                        parentColumns = ["id"],
                        childColumns = ["parentId"],
                        deferred = true
                )
        ]
)
data class Definition(
        @PrimaryKey
        var id: String = "",
        val parentId: String,
        val parentWord: String,
        val date: String,
        val definitions: List<OrderedDefinitionItem>
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Definition) return false
        if (this === other) return true

        return id == other.id &&
                parentId == other.parentId &&
                parentWord == other.parentWord &&
                definitions.containsAll(other.definitions)
//        Date excluded!
    }

    override fun toString(): String {
        return "$id, $parentId, $parentWord, $date, $definitions"
    }
}
