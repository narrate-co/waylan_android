package space.narrate.waylan.android.ui.search

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import space.narrate.waylan.android.R
import space.narrate.waylan.android.ui.search.ShelfActionModel.CloseKeyboardAction
import space.narrate.waylan.android.ui.search.ShelfActionModel.CloseSheetAction
import space.narrate.waylan.android.ui.search.ShelfActionModel.FavoriteAction
import space.narrate.waylan.android.ui.search.ShelfActionModel.FilterAction
import space.narrate.waylan.android.ui.search.ShelfActionModel.ShareAction
import space.narrate.waylan.android.ui.search.ShelfActionModel.UnfavoriteAction
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.users.isFavorited
import space.narrate.waylan.core.ui.common.Diffable

/**
 * A model class that holds information about what actions should be displayed in the 'row'
 * area to the right of the search area. This area can hold up to two actions. The actions displayed
 * changed contextually depending on the current screen and the state of any bottom sheets.
 */
sealed class SearchShelfActionRowModel : Diffable<SearchShelfActionRowModel> {
    
    abstract val numberOfActionsToShow: Int
    abstract val shouldAnimateToNumberOfActions: Boolean

    open val actionOne: ShelfActionModel? = null
    open val actionTwo: ShelfActionModel? = null

    override fun isSameAs(newOther: SearchShelfActionRowModel): Boolean {
        return this === newOther
    }

    class DetailsShelfActions(
        userWord: UserWord
    ) : SearchShelfActionRowModel() {

        override val numberOfActionsToShow: Int = 2
        override val shouldAnimateToNumberOfActions: Boolean = true

        val isFavorited: Boolean = userWord.isFavorited
        override val actionOne = if (isFavorited) UnfavoriteAction else FavoriteAction
        override val actionTwo = ShareAction

        override fun isContentSameAs(newOther: SearchShelfActionRowModel): Boolean {
            if (newOther !is DetailsShelfActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && isFavorited == newOther.isFavorited
                && actionOne.isContentSameAs(newOther.actionOne)
                && actionTwo.isContentSameAs(newOther.actionTwo)
        }
    }

    class ListShelfActions(val hasFilter: Boolean) : SearchShelfActionRowModel() {

        override val numberOfActionsToShow: Int = if (hasFilter) 0 else 1
        override val shouldAnimateToNumberOfActions: Boolean = true

        override val actionOne = FilterAction

        override fun isContentSameAs(newOther: SearchShelfActionRowModel): Boolean {
            if (newOther !is ListShelfActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && hasFilter == newOther.hasFilter
                && actionOne.isContentSameAs(newOther.actionOne)
        }
    }

    class SheetKeyboardControllerActions(
        val isSheetExpanded: Boolean,
        val isKeyboardOpen: Boolean
    ) : SearchShelfActionRowModel() {

        override val numberOfActionsToShow: Int = if (isSheetExpanded || isKeyboardOpen) 1 else 0
        override val shouldAnimateToNumberOfActions: Boolean = false

        override val actionOne = if (isKeyboardOpen) CloseKeyboardAction else CloseSheetAction

        override fun isContentSameAs(newOther: SearchShelfActionRowModel): Boolean {
            if (newOther !is SheetKeyboardControllerActions) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
                && actionOne.isContentSameAs(newOther.actionOne)
        }
    }

    class None : SearchShelfActionRowModel() {
        override val numberOfActionsToShow: Int = 0
        override val shouldAnimateToNumberOfActions: Boolean = true

        override fun isContentSameAs(newOther: SearchShelfActionRowModel): Boolean {
            if (newOther !is None) return false
            return numberOfActionsToShow == newOther.numberOfActionsToShow
        }
    }
}

/**
 * A class whcih models a single action to be displayed in the action row area.
 */
sealed class ShelfActionModel(
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
    @DrawableRes val backgroundDrawable: Int? = null
): Diffable<ShelfActionModel> {
    override fun isSameAs(newOther: ShelfActionModel): Boolean {
        return this === newOther
    }
    override fun isContentSameAs(newOther: ShelfActionModel): Boolean {
        return icon == newOther.icon && contentDescription == newOther.contentDescription
    }

    object ShareAction : ShelfActionModel(
        R.drawable.ic_round_share_24px,
        R.string.search_shelf_action_share_content_desc
    )

    object FavoriteAction : ShelfActionModel(
        R.drawable.ic_round_favorite_border_24px,
        R.string.search_shelf_action_favorite_content_desc
    )

    object UnfavoriteAction : ShelfActionModel(
        R.drawable.ic_round_favorite_24px,
        R.string.search_shelf_action_unfavorite_content_desc
    )

    object FilterAction : ShelfActionModel(
        R.drawable.ic_round_filter_list_24px,
        R.string.search_shelf_action_filter_content_desc
    )

    object CloseSheetAction : ShelfActionModel(
        R.drawable.ic_keyboard_arrow_down_24px,
        R.string.search_shelf_action_sheet_close_content_desc,
        R.drawable.search_input_area
    )

    object CloseKeyboardAction : ShelfActionModel(
        R.drawable.ic_keyboard_hide_black_24dp,
        R.string.search_shelf_action_keyboard_down_content_desc,
        R.drawable.search_input_area
    )
}

