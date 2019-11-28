package space.narrate.waylan.android.util

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import space.narrate.waylan.android.R
import space.narrate.waylan.android.data.disk.wordset.Synonym
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.core.ui.widget.ElasticAppBarBehavior
import space.narrate.waylan.core.util.setUpWithElasticBehavior
import space.narrate.waylan.core.util.toChip

fun AppBarLayout.setUpWithElasticBehavior(
    currentDestination: String,
    sharedViewModel: MainViewModel,
    parallaxOnDrag: List<View>,
    alphaOnDrag: List<View>
) {
    val callback = object : ElasticAppBarBehavior.ElasticViewBehaviorCallback {
        override fun onDrag(
            dragFraction: Float,
            dragTo: Float,
            rawOffset: Float,
            rawOffsetPixels: Float,
            dragDismissScale: Float
        ) {
            val alpha = 1 - dragFraction
            val cutDragTo = dragTo * .15F

            parallaxOnDrag.forEach { it.translationY = cutDragTo }
            alphaOnDrag.forEach { it.alpha = alpha }
        }

        override fun onDragDismissed(): Boolean {
            return sharedViewModel.onDragDismissBackEvent(currentDestination)
        }
    }

    setUpWithElasticBehavior(callback)
}

fun Synonym.toChip(
    context: Context,
    chipGroup: ChipGroup?,
    onClick: ((synonym: Synonym) -> Unit)? = null
): Chip {
   return synonym.toChip(context, chipGroup) {
        if (onClick != null && it == synonym) onClick(this)
    }
}
