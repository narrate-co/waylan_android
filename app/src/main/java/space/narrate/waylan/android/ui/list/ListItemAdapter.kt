package space.narrate.waylan.android.ui.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.android.util.AdapterUtils
import space.narrate.waylan.android.ui.widget.BannerCardView
import java.lang.IllegalArgumentException

class ListItemAdapter(
        private val listener: ListItemListener
) : ListAdapter<ListItemModel, ListItemViewHolder<ListItemModel>>(
    AdapterUtils.diffableItemCallback()
) {

    interface ListItemListener: BannerCardView.Listener {
        /**
         * Called when an item is clicked, passing that item's [word] as it appears in the
         * dictionary, or when an item's inner view is clicked that contains a word, such as a
         * synonym chip, again passing that inner view's [word] as it appears in the dictionary.
         */
        fun onWordClicked(word: String)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItemModel.HeaderModel -> VIEW_TYPE_HEADER
            is ListItemModel.UserWordModel -> VIEW_TYPE_USER_WORD
            is ListItemModel.GlobalWordModel -> VIEW_TYPE_GLOBAL_WORD
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ListItemViewHolder<ListItemModel> {
        return when (viewType) {
            VIEW_TYPE_HEADER -> ListItemViewHolder.HeaderViewHolder(parent, listener)
            VIEW_TYPE_USER_WORD -> ListItemViewHolder.UserWordViewHolder(parent, listener)
            VIEW_TYPE_GLOBAL_WORD -> ListItemViewHolder.GlobalWordViewHolder(parent, listener)
            else ->
                throw IllegalArgumentException("Unsupported viewType being inflated - $viewType")
        } as ListItemViewHolder<ListItemModel>
    }

    override fun onBindViewHolder(holder: ListItemViewHolder<ListItemModel>, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 10
        private const val VIEW_TYPE_USER_WORD = 11
        private const val VIEW_TYPE_GLOBAL_WORD = 12
    }
}

