package space.narrate.waylan.settings.ui.addons

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException

/**
 * A RecyclerView Scroll Adapter that configures a RecyclerView to snap to each item in its list
 * and is able to report snapped positions through [OnPageChangedCallback].
 */
class SnapScrollEventAdapter(
    recyclerView: RecyclerView
) : RecyclerView.OnScrollListener() {

    interface OnPageChangedCallback {
        fun onPageSelected(position: Int)
    }

    private val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        ?: throw IllegalArgumentException("SnapScrollEventAdapter's RecyclerView must use " +
            "a LienarLayoutManager.")

    private var callback: OnPageChangedCallback? = null

    init {
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
    }

    private var currentPage = RecyclerView.NO_POSITION

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        currentPage = layoutManager.findFirstVisibleItemPosition()
    }
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            dispatchOnPageChanged()
        }
    }

    private fun dispatchOnPageChanged() {
        callback?.onPageSelected(currentPage)
    }

    fun setOnPageChangedCallback(callback: OnPageChangedCallback?) {
        this.callback = callback
    }
}