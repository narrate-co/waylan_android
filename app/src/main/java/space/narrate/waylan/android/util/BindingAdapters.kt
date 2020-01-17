package space.narrate.waylan.android.util

import android.content.Context
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import space.narrate.waylan.android.R

@BindingAdapter(
    "behavior_isSearchSheet"
)
fun View.bindBehaviorIsSearchSheet(
    isSearchSheet: Boolean
) {
    if (!isSearchSheet) return

    doOnApplyWindowInsets { view, insets, _ ->
        val behavior = BottomSheetBehavior.from(view)
        behavior.peekHeight = SheetDimens.getSearchPeekHeight(view.context, insets)
        // Set updated behavior on the view's LayoutParams
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = behavior
    }
}

@BindingAdapter(
    "behavior_isContextualSheet"
)
fun View.bindBehaviorIsContextualSheet(isContextualSheet: Boolean) {
    if (!isContextualSheet) return

    doOnApplyWindowInsets { view, insets, _ ->
        val behavior = BottomSheetBehavior.from(view)

        view.updatePadding(bottom = SheetDimens.getSearchPeekHeight(view.context, insets))
        behavior.peekHeight = SheetDimens.getContextualPeekHeight(view.context, insets)
        // Set updated behavior on the view's LayoutParams
        val params = view.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = behavior
    }
}

@BindingAdapter(
    "paddingBottomAboveSearchSheet"
)
fun View.bindPaddingBottomAboveSearchSheet(
    paddingBottomAboveSearchSheet: Boolean
) {
    doOnApplyWindowInsets { view, insets, initialState ->
        if (paddingBottomAboveSearchSheet) {
            val searchSheetHeight = SheetDimens.getSearchPeekHeight(view.context, insets)
            val totalPadding = initialState.paddings.bottom + searchSheetHeight

            view.updatePadding(bottom = totalPadding)
        }
    }
}

/**
 * Constant helper functions to get the summed height of UI components which other pieces of
 * UI are dependent on.
 */
object SheetDimens {

    private fun getSheetBottomSystemWindowInset(context: Context, insets: WindowInsetsCompat): Int {
        return insets.systemWindowInsetBottom +
            context.resources.getDimensionPixelSize(R.dimen.keyline_2)
    }

    fun getSearchPeekHeight(context: Context, insets: WindowInsetsCompat): Int {
        return context.resources.getDimensionPixelSize(
            R.dimen.search_min_peek_height
        ) + getSheetBottomSystemWindowInset(context, insets)
    }


    fun getContextualPeekHeight(context: Context, insets: WindowInsetsCompat): Int {
        return getSearchPeekHeight(context, insets) +
            context.resources.getDimensionPixelSize(
                R.dimen.contextual_collapsed_container_height
            )
    }

}
