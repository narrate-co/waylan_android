package space.narrate.words.android.ui.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * A generic [ListAdapter] that supports adding, changing and removing a header item view which
 * differs from the list's main type T
 *
 * @param T Type of Lists this adapter will receive
 * @param VH A class that extends ViewHolder that will be used by the adapter
 * @param H The type of object this adapter's header will be
 */
abstract class HeaderListAdapter<T, VH : RecyclerView.ViewHolder, H>(
        diffCallback: DiffUtil.ItemCallback<T>
): ListAdapter<T, VH>(diffCallback) {

    companion object {
        const val VIEW_TYPE_HEADER = 8
        const val VIEW_TYPE_ITEM = 9
    }


    private var header: H? = null

    fun getHeader(): H? {
        return header
    }

    private fun getHeaderOffset(): Int {
        return if (header != null) 1 else 0
    }

    fun setHeader(header: H?) {
        if (this.header == null && header != null) {
            //we're inserting the header
            this.header = header
            notifyItemInserted(0)
        } else if (this.header != null && header == null) {
            //we're removing the header
            this.header = header
            notifyItemRemoved(0)
        } else if (this.header != null && header != null) {
            //we're changing the header
            this.header = header
            notifyItemChanged(0)
        } // else nothing has changed. The banner was, and still is, null.
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && header != null) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + getHeaderOffset()
    }

    override fun getItem(position: Int): T {
        return super.getItem(position - getHeaderOffset())
    }
}