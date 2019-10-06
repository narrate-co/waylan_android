package space.narrate.waylan.core.details

import space.narrate.waylan.core.ui.common.Diffable

/**
 * A data class which can be displayed by a [DetailItemViewHolder]. Each module which wishes
 * to have data displayed in a word's details screen should implement it's own [DetailItemModel].
 */
abstract class DetailItemModel : Diffable<DetailItemModel> {
    abstract val itemType: DetailItemType
}
