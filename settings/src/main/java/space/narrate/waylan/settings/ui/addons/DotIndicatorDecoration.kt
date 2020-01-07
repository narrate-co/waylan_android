package space.narrate.waylan.settings.ui.addons

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.suspendAtomicCancellableCoroutine
import space.narrate.waylan.core.util.getColorFromAttr
import space.narrate.waylan.settings.R

/**
 * A [RecyclerView.ItemDecoration] that draws dots for each item in the [RecyclerView], centered
 * at the bottom of the [RecyclerView], with the currently visible item denoted with a darker
 * dot.
 */
class DotIndicatorDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val dotRadius = context.resources.getDimension(R.dimen.dot_indicator_dot_radius)
    private val dotSpacing = context.resources.getDimension(R.dimen.dot_indicator_dot_spacing)
    private val dotVerticalOffset = context.resources.getDimension(R.dimen.keyline_2)

    private val inactiveDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColorStateList(R.color.material_on_background_disabled)
            .defaultColor
    }
    private val activeDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColorStateList(R.color.material_on_background_emphasis_medium)
            .defaultColor
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: RecyclerView.NO_POSITION
        val currentItem = when (val manager = parent.layoutManager) {
            is LinearLayoutManager -> manager.findFirstVisibleItemPosition()
            is GridLayoutManager -> manager.findFirstVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }

        // Exit early if there are not items or current items
        if (itemCount == RecyclerView.NO_POSITION || currentItem == RecyclerView.NO_POSITION) return

        val dotDiameter = dotRadius * 2F
        val dotContainerWidth = (itemCount * dotDiameter) + (dotSpacing * (itemCount - 1))
        val parentCenterX = (parent.right - parent.left) / 2F
        val dotContainer = RectF(
            parentCenterX - (dotContainerWidth / 2F),
            parent.bottom - dotVerticalOffset - dotDiameter,
            parentCenterX + (dotContainerWidth / 2F),
            parent.bottom - dotVerticalOffset
        )

        (0 until itemCount).forEach { position ->
            val left = position * (dotDiameter + dotSpacing)
            val centerX = dotContainer.left + (left + dotRadius)
            val paint = if (position == currentItem) activeDotPaint else inactiveDotPaint
            c.drawCircle(centerX, dotContainer.centerY(), dotRadius, paint)
        }
    }
}