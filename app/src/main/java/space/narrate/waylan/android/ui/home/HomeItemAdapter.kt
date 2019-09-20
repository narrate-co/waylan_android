package space.narrate.waylan.android.ui.home

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.android.util.AdapterUtils

class HomeItemAdapter(
    private val listener: HomeItemListener
) : ListAdapter<HomeItemModel, HomeItemViewHolder<HomeItemModel>>(
    AdapterUtils.diffableItemCallback()
) {

    interface HomeItemListener {
        fun onItemClicked(item: HomeItemModel.ItemModel)
        fun onSettingsClicked()
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeItemModel.ItemModel -> VIEW_TYPE_ITEM
            is HomeItemModel.DividerModel -> VIEW_TYPE_DIVIDER
            is HomeItemModel.SettingsModel -> VIEW_TYPE_SETTINGS
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeItemViewHolder<HomeItemModel> {
        @Suppress("UNCHECKED_CAST")
        return when (viewType) {
            VIEW_TYPE_ITEM -> HomeItemViewHolder.ItemViewHolder(parent, listener)
            VIEW_TYPE_DIVIDER -> HomeItemViewHolder.DividerViewHolder(parent)
            VIEW_TYPE_SETTINGS -> HomeItemViewHolder.SettingsViewHolder(parent, listener)
            else ->
                throw IllegalArgumentException("Unsupported viewType being inflated - $viewType")
        } as HomeItemViewHolder<HomeItemModel>
    }

    override fun onBindViewHolder(holder: HomeItemViewHolder<HomeItemModel>, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 14
        private const val VIEW_TYPE_DIVIDER = 15
        private const val VIEW_TYPE_SETTINGS = 16
    }
}