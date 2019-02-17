package space.narrate.words.android.data.disk.mw

/**
 * A class that ensures a [def] is always associated with the same [sn] when returned
 * from the Merriam-Webster API.
 *
 * Responses from Merriam-Webster do not ensure order in their definitions list. To help diffing
 * the local Merriam-Webster RooomDatbase and responses received from the API, [sn] acts as a
 * weak "id" of sorts.
 */
data class OrderedDefinitionItem(
     val sn: String,
     val def: String
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is OrderedDefinitionItem) return false
        if (other === this) return true

        return sn == other.sn && def == other.def
    }
}

