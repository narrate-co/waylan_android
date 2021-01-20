package space.narrate.waylan.android.ui.details

import space.narrate.waylan.core.data.wordset.WordAndMeanings
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

class WaylanDefinitionsModel(val entry: WordAndMeanings): DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.DEFINITION

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is WaylanDefinitionsModel) return false
        return this == newOther
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is WaylanDefinitionsModel) return false
        return entry.meanings
            .map { it.def }
            .toTypedArray()
            .contentDeepEquals(
                newOther.entry.meanings
                    .map { it.def }
                    .toTypedArray()
            )
    }
}
