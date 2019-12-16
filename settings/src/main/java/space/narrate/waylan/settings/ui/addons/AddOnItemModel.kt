package space.narrate.waylan.settings.ui.addons

import androidx.annotation.StringRes
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.merriamWebsterState
import space.narrate.waylan.core.data.firestore.users.merriamWebsterThesaurusState
import space.narrate.waylan.core.ui.common.Diffable
import space.narrate.waylan.settings.R
import java.util.*

sealed class AddOnItemModel(
    @StringRes val title: Int,
    @StringRes val desc: Int,
    val state: PluginState
) : Diffable<AddOnItemModel> {

    class MerriamWebster(user: User) : AddOnItemModel(
        R.string.add_ons_merriam_webster_title,
        R.string.add_ons_merriam_webster_desc,
        user.merriamWebsterState
    )

    class MerriamWebsterThesaurus(user: User) : AddOnItemModel(
        R.string.add_ons_merriam_webster_thesaurus_title,
        R.string.add_ons_merriam_webster_thesaurus_desc,
        user.merriamWebsterThesaurusState
    )

    override fun isSameAs(newOther: AddOnItemModel): Boolean = this == newOther

    override fun isContentSameAs(newOther: AddOnItemModel): Boolean =
        this.title == newOther.title
            && this.desc == newOther.desc
            && this.state == newOther.state
}

