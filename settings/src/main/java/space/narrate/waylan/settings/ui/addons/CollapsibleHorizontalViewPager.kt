package space.narrate.waylan.settings.ui.addons

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class CollapsibleHorizontalViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val result = super.onTouchEvent(e)
        parent.requestDisallowInterceptTouchEvent(false)
        return result
    }

}