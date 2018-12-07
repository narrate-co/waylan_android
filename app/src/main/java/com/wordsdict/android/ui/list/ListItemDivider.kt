package com.wordsdict.android.ui.list

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.R
import com.wordsdict.android.util.children

/**
 * A [RecyclerView.ItemDecoration] that draws a divider above all items in a RecyclerView
 * except for the first item. Effectively only drawing a divider <i>between</i> items
 */
class ListItemDivider(private val drawable: Drawable?): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val isHeader = view.findViewWithTag<View>("header") != null ?: false
        if (parent.getChildAdapterPosition(view) == 0 || drawable == null || isHeader) return

        outRect.set(0,drawable.intrinsicHeight,0,0)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (drawable == null) return

        val dividerLeft = parent.paddingLeft + parent.context.resources.getDimensionPixelOffset(R.dimen.keyline_3)
        val dividerRight = parent.width - parent.paddingRight

        parent.children.forEach { child ->
            val params: RecyclerView.LayoutParams = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + drawable.intrinsicHeight

            if (child.findViewWithTag<View>("header") == null) {
                drawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                drawable.draw(c)
            }
        }
    }
}

