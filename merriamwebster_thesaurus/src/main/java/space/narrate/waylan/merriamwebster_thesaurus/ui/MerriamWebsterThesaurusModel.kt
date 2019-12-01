package space.narrate.waylan.merriamwebster_thesaurus.ui

import space.narrate.waylan.android.data.firestore.users.User
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry

/**
 * Merriam-Webster Thesaurus' [DetailItemModel] which is used to construct a DetailItemViewHolder
 * that is able to be displayed by the details screen.
 */
class MerriamWebsterThesaurusModel(
    val entries: List<ThesaurusEntry>,
    val user: User?
) : DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.MERRIAM_WEBSTER_THESAURUS

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterThesaurusModel) return false
        return entries == newOther.entries && user == newOther.user
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is MerriamWebsterThesaurusModel) return false

        // TODO: Add checks for user equality checks after mwt user permission are added.
        return entries.toTypedArray().contentDeepEquals(newOther.entries.toTypedArray())
    }

}