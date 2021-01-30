package space.narrate.waylan.android.ui.list

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.android.databinding.ListBannerLayoutBinding
import space.narrate.waylan.android.databinding.ListItemLayoutBinding
import space.narrate.waylan.core.ui.widget.BannerCardView
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.core.util.inflater

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
         *
         * @param word The string of the word that was clicked on
         * @param view The container view of the list item that was clicked
         * @param useSharedElement Whether or not there should be a shared element transition
         *  between the [view] and the word's details page. This will be false if a synonym or other
         *  minor element of the container was clicked and doesn't have enough content to warrant
         *  a shared element transition.
         */
        fun onWordClicked(word: String, view: View, useSharedElement: Boolean)
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
        @Suppress("UNCHECKED_CAST")
        return when (viewType) {
            VIEW_TYPE_HEADER -> ListItemViewHolder.HeaderViewHolder(
                ListBannerLayoutBinding.inflate(parent.inflater, parent, false),
                listener
            )
            VIEW_TYPE_USER_WORD -> ListItemViewHolder.UserWordViewHolder(
                ListItemLayoutBinding.inflate(parent.inflater, parent, false),
                listener
            )
            VIEW_TYPE_GLOBAL_WORD -> ListItemViewHolder.GlobalWordViewHolder(
                ListItemLayoutBinding.inflate(parent.inflater, parent, false),
                listener
            )
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

