package com.wordsdict.android.ui.list

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wordsdict.android.R
import com.wordsdict.android.util.children

class ListItemDivider(private val drawable: Drawable?): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) == 0 || drawable == null) return

        outRect.top = drawable.intrinsicHeight
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (drawable == null) return

        val dividerLeft = parent.paddingLeft + parent.context.resources.getDimensionPixelOffset(R.dimen.keyline_3)
        val dividerRight = parent.width - parent.paddingRight

        parent.children.forEach { child ->
            val params: RecyclerView.LayoutParams = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + drawable.intrinsicHeight

            drawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            drawable.draw(c)
        }
    }
}

