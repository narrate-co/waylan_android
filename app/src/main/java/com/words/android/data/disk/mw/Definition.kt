package com.words.android.data.disk.mw

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "mw_definitions",
        indices = [(Index("parentWord"))],
        foreignKeys = [
                ForeignKey(
                        entity = Word::class,
                        parentColumns = ["word"],
                        childColumns = ["parentWord"],
                        deferred = true
                )
        ]
)
data class Definition(
        @PrimaryKey
        var id: String = "",
        val parentWord: String,
        val date: String,
        val sns: List<String>,
        val defs: List<String>
)
