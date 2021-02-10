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
        val oldPosition = currentPage
        currentPage = layoutManager.findFirstVisibleItemPosition()
        // dispatch the first time the adapter is populated with items. onScrollStateChanged
        // will not be called. We have to manually watch and dispatch for the attachment
        // event so any listeners will receive a callback and be able to update any values when
        // the adapter is first populated.
        if (oldPosition == RecyclerView.NO_POSITION && currentPage != oldPosition) {
            dispatchOnPageChanged()
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            dispatchOnPageChanged()
        }
    }

    private fun dispatchOnPageChanged() {
        if (currentPage != RecyclerView.NO_POSITION) {
            callback?.onPageSelected(currentPage)
        }
    }

    fun setOnPageChangedCallback(callback: OnPageChangedCallback?) {
        this.callback = callback
    }
}