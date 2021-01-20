package space.narrate.waylan.android.ui.details

import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

class WaylanExamplesModel(val examples: List<Example>): DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.EXAMPLE

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is WaylanExamplesModel) return false
        return this == newOther
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is WaylanExamplesModel) return false
        return examples.toTypedArray().contentDeepEquals(examples.toTypedArray())
    }
}
