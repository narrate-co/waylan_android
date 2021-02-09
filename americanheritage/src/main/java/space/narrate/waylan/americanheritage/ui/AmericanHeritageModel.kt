package space.narrate.waylan.americanheritage.ui

import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.remainingDays
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.wordnik.data.local.Definition

/**
 * American Heritage's [DetailItemModel] which is used to construct a view model that can
 * be used to display an entry in the details screen.
 */
class AmericanHeritageModel(
  val definitions: List<Definition>,
  val userAddOn: UserAddOn?
) : DetailItemModel() {

  override val itemType: DetailItemType = DetailItemType.AMERICAN_HERITAGE

  override fun isSameAs(newOther: DetailItemModel): Boolean {
    if (newOther !is AmericanHeritageModel) return false
    return  definitions == newOther.definitions && userAddOn == newOther.userAddOn
  }

  override fun isContentSameAs(newOther: DetailItemModel): Boolean {
    if (newOther !is AmericanHeritageModel) return false
    return definitions.toTypedArray().contentDeepEquals(newOther.definitions.toTypedArray())
      && userAddOn?.state == newOther.userAddOn?.state
      && userAddOn?.remainingDays == newOther.userAddOn?.remainingDays
  }
}