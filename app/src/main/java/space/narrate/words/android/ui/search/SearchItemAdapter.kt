package space.narrate.words.android.ui.search

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.words.android.util.AdapterUtils
import space.narrate.words.android.util.widget.BannerCardView

class SearchItemAdapter(
        private val listener: SearchItemListener
): ListAdapter<SearchItemModel, SearchItemViewHolder<SearchItemModel>>(
    AdapterUtils.diffableItemCallback()
) {

    interface SearchItemListener: BannerCardView.Listener {
        /**
         * Called when an item is clicked, passing that item's [word] as it appears in
         * the dictionary
         */
        fun onWordClicked(searchItem: SearchItemModel)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SearchItemModel.HeaderModel -> VIEW_TYPE_HEADER
            is SearchItemModel.WordModel -> VIEW_TYPE_WORD
            is SearchItemModel.UserWordModel -> VIEW_TYPE_USER_WORD
            is SearchItemModel.SuggestModel -> VIEW_TYPE_SUGGEST
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchItemViewHolder<SearchItemModel> {
        return when (viewType) {
            VIEW_TYPE_HEADER -> SearchItemViewHolder.HeaderViewHolder(parent, listener)
            VIEW_TYPE_WORD -> SearchItemViewHolder.WordViewHolder(parent, listener)
            VIEW_TYPE_USER_WORD -> SearchItemViewHolder.UserWordViewHolder(parent, listener)
            VIEW_TYPE_SUGGEST -> SearchItemViewHolder.SuggestViewHolder(parent, listener)
            else ->
                throw IllegalArgumentException("Unspupported viewType being inflated - $viewType")
        } as SearchItemViewHolder<SearchItemModel>
    }

    override fun onBindViewHolder(holder: SearchItemViewHolder<SearchItemModel>, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 2
        private const val VIEW_TYPE_WORD = 3
        private const val VIEW_TYPE_USER_WORD = 4
        private const val VIEW_TYPE_SUGGEST = 5
    }
}