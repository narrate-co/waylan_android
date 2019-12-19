package space.narrate.waylan.merriamwebster.ui

import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.remainingDays
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups

/**
 * Merriam-Webster's [DetailItemModel] which is used to construct a DetailItemViewHolder that
 * is able to be displayed by the details screen.
 * TODO: Add UserAddOn instead of user object
 */
class MerriamWebsterModel(
    val entries: List<MwWordAndDefinitionGroups>,
    val userAddOn: UserAddOn?
) : DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterModel) return false
        return entries == newOther.entries && userAddOn == newOther.userAddOn
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is MerriamWebsterModel) return false

        return entries.toTypedArray().contentDeepEquals(newOther.entries.toTypedArray())
            && userAddOn?.state == newOther.userAddOn?.state
            && userAddOn?.remainingDays == newOther.userAddOn?.remainingDays
    }
}