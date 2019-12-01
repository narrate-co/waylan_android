package space.narrate.waylan.merriamwebster.ui

import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups
import space.narrate.waylan.android.data.firestore.users.User
import space.narrate.waylan.android.data.firestore.users.merriamWebsterState
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

/**
 * Merriam-Webster's [DetailItemModel] which is used to construct a DetailItemViewHolder that
 * is able to be displayed by the details screen.
 */
class MerriamWebsterModel(
    val entries: List<MwWordAndDefinitionGroups>,
    val user: User?
) : DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterModel) return false
        return entries == newOther.entries && user == newOther.user
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is MerriamWebsterModel) return false

        return entries.toTypedArray().contentDeepEquals(newOther.entries.toTypedArray()) &&
            user?.merriamWebsterState == newOther.user?.merriamWebsterState &&
            user?.merriamWebsterState?.remainingDays ==
            newOther.user?.merriamWebsterState?.remainingDays
    }
}