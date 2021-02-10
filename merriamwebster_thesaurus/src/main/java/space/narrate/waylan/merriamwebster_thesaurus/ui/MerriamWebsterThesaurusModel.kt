package space.narrate.waylan.merriamwebster_thesaurus.ui

import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.remainingDays
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry

/**
 * Merriam-Webster Thesaurus' [DetailItemModel] which is used to construct a DetailItemViewHolder
 * that is able to be displayed by the details screen.
 */
class MerriamWebsterThesaurusModel(
    val entries: List<ThesaurusEntry>,
    val userAddOn: UserAddOn?
) : DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER_THESAURUS

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterThesaurusModel) return false
        return entries == newOther.entries && userAddOn == newOther.userAddOn
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterThesaurusModel) return false

        return entries.toTypedArray().contentDeepEquals(newOther.entries.toTypedArray())
            && userAddOn?.state == newOther.userAddOn?.state
            && userAddOn?.remainingDays == newOther.userAddOn?.remainingDays
    }
}