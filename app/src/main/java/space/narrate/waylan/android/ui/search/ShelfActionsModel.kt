package space.narrate.waylan.android.ui.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.users.UserWordType
import space.narrate.waylan.core.data.firestore.users.isFavorited
import space.narrate.waylan.core.ui.common.Diffable

sealed class SearchShelfActionsModel : Diffable<SearchShelfActionsModel> {
    
    abstract val numberOfActionsToShow: Int

    override fun isSameAs(newOther: SearchShelfActionsModel): Boolean {
        return this === newOther
    }

    class DetailsShelfActions(
        userWord: UserWord
    ) : SearchShelfActionsModel() {

        override val numberOfActionsToShow: Int = 2


        val isFavorited: Boolean = userWord.isFavorited
        val actionOne = if (isFavorited) favoritedAction else unfavoritedAction
        val actionTwo = ShelfAction(
            R.drawable.ic_round_share_24px,
            R.string.search_shelf_action_share_content_desc
        )

        override fun isContentSameAs(newOther: SearchShelfActionsModel): Boolean {
            if (newOther !is DetailsShelfActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && isFavorited == newOther.isFavorited
                && actionOne.isContentSameAs(newOther.actionOne)
                && actionTwo.isContentSameAs(newOther.actionTwo)
        }

        companion object {
            private val favoritedAction = ShelfAction(
                R.drawable.ic_round_favorite_24px,
                R.string.search_shelf_action_unfavorite_content_desc
            )
            private val unfavoritedAction = ShelfAction(
                R.drawable.ic_round_favorite_border_24px,
                R.string.search_shelf_action_favorite_content_desc
            )
        }
    }

    class ListShelfActions(val hasFilter: Boolean) : SearchShelfActionsModel() {

        override val numberOfActionsToShow: Int = if (hasFilter) 0 else 1

        val actionOne = ShelfAction(
            R.drawable.ic_round_filter_list_24px,
            R.string.search_shelf_action_filter_content_desc
        )

        override fun isContentSameAs(newOther: SearchShelfActionsModel): Boolean {
            if (newOther !is ListShelfActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && hasFilter == newOther.hasFilter
                && actionOne.isContentSameAs(newOther.actionOne)
        }
    }

    class SheetKeyboardControllerActions(
        val isSheetExpanded: Boolean,
        val isKeyboardOpen: Boolean
    ) : SearchShelfActionsModel() {

        override val numberOfActionsToShow: Int = if (isSheetExpanded || isKeyboardOpen) 1 else 0

        val actionOne = if (isKeyboardOpen) closeKeyboardAction else closeSheetAction

        override fun isContentSameAs(newOther: SearchShelfActionsModel): Boolean {
            if (newOther !is SheetKeyboardControllerActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && actionOne.isContentSameAs(newOther.actionOne)
        }

        companion object {
            private val closeSheetAction = ShelfAction(
                R.drawable.ic_keyboard_arrow_down_24px,
                R.string.search_shelf_action_sheet_close_content_desc
            )
            private val closeKeyboardAction = ShelfAction(
                R.drawable.ic_keyboard_hide_black_24dp,
                R.string.search_shelf_action_keyboard_down_content_desc
            )
        }
    }

    class None : SearchShelfActionsModel() {
        override val numberOfActionsToShow: Int = 0

        override fun isContentSameAs(newOther: SearchShelfActionsModel): Boolean {
            if (newOther !is None) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
        }
    }
}

data class ShelfAction(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int
): Diffable<ShelfAction> {
    override fun isSameAs(newOther: ShelfAction): Boolean {
        return this === newOther
    }
    override fun isContentSameAs(newOther: ShelfAction): Boolean {
        return icon == newOther.icon && contentDescription == newOther.contentDescription
    }
}

