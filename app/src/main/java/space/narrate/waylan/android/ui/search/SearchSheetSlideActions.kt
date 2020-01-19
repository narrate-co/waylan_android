package space.narrate.waylan.android.ui.search

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import space.narrate.waylan.android.R
import space.narrate.waylan.android.util.OnSlideAction
import space.narrate.waylan.core.util.MathUtils

class PinSearchAreaSlideAction(private val searchArea: View) : OnSlideAction {

    private var sheetBehavior: BottomSheetBehavior<out View>? = null

    private fun getSheetPeekHeight(sheet: View): Int {
        if (sheetBehavior == null) {
            sheetBehavior = BottomSheetBehavior.from(sheet)
        }
        return sheetBehavior?.peekHeight ?: 0
    }

    override fun onSlide(sheet: View, slideOffset: Float) {
        val delta = (sheet.height - getSheetPeekHeight(sheet)) * slideOffset
        // Adjust the margins of the search container to allow the search results
        // recyclerview to recalculate its available space and increase its height.
        val params = searchArea.layoutParams as ConstraintLayout.LayoutParams
        val originalTopMargin = searchArea.resources
            .getDimensionPixelSize(R.dimen.search_input_area_margin_top)
        params.updateMargins(top = delta.toInt() + originalTopMargin)
        searchArea.layoutParams = params
    }
}

class FadeInOutSearchResultsSlideAction(private val recyclerView: RecyclerView) : OnSlideAction {
    override fun onSlide(sheet: View, slideOffset: Float) {
        recyclerView.alpha = MathUtils.normalize(
            slideOffset,
            0.2F,
            1.0F,
            0.0F,
            1.0F
        )
    }
}