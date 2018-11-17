package com.wordsdict.android.data.disk.mw

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

