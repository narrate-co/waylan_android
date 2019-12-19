package space.narrate.waylan.settings.ui.addons

import androidx.annotation.StringRes
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.AddOnAction
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.ui.common.Diffable
import space.narrate.waylan.settings.R

/**
 * A UI model used to show an [AddOn] and the state of that add on for the current user.
 */
sealed class AddOnItemModel(
    @StringRes val title: Int,
    @StringRes val desc: Int,
    val addOn: AddOn,
    val userAddOn: UserAddOn
) : Diffable<AddOnItemModel> {

    class MerriamWebster(addOn: AddOn, userAddOn: UserAddOn) : AddOnItemModel(
        R.string.add_ons_merriam_webster_title,
        R.string.add_ons_merriam_webster_desc,
        addOn,
        userAddOn
    )

    class MerriamWebsterThesaurus(addOn: AddOn, userAddOn: UserAddOn) : AddOnItemModel(
        R.string.add_ons_merriam_webster_thesaurus_title,
        R.string.add_ons_merriam_webster_thesaurus_desc,
        addOn,
        userAddOn
    )

    override fun isSameAs(newOther: AddOnItemModel): Boolean = this == newOther

    override fun isContentSameAs(newOther: AddOnItemModel): Boolean =
        this.title == newOther.title
            && this.desc == newOther.desc
            && this.addOn == newOther.addOn
            && this.userAddOn.validDurationDays == newOther.userAddOn.validDurationDays
            && this.userAddOn.hasStartedFreeTrial == newOther.userAddOn.hasStartedFreeTrial
            && this.userAddOn.started == newOther.userAddOn.started
            && this.userAddOn.purchaseToken == newOther.userAddOn.purchaseToken
}

