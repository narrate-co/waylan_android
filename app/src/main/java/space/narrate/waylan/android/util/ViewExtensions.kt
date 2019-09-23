package space.narrate.waylan.android.util

import android.view.View
import com.google.android.material.appbar.AppBarLayout
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.core.ui.widget.ElasticAppBarBehavior
import space.narrate.waylan.core.util.setUpWithElasticBehavior

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


