package space.narrate.waylan.android.home

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import space.narrate.waylan.android.R
import space.narrate.waylan.core.util.AdapterUtils

sealed class HomeItemViewHolder<T : HomeItemModel>(val view: View) : RecyclerView.ViewHolder(view) {

    open fun bind(item: T) { }

    class ItemViewHolder(
        parent: ViewGroup,
        private val listener: HomeItemAdapter.HomeItemListener
    ) : HomeItemViewHolder<HomeItemModel.ItemModel>(
        AdapterUtils.inflate(parent, R.layout.home_item_layout)
    ) {
        private val titleTextView: AppCompatTextView = view.findViewById(R.id.title_text_view)
        private val previewTextView: AppCompatTextView = view.findViewById(R.id.preview_text_view)

        override fun bind(item: HomeItemModel.ItemModel) {
            titleTextView.text = view.context.getString(item.titleRes)
            previewTextView.text = item.preview
            view.setOnClickListener { listener.onItemClicked(item) }
        }
    }

    class DividerViewHolder(
        parent: ViewGroup
    ) : HomeItemViewHolder<HomeItemModel.DividerModel>(
        AdapterUtils.inflate(parent, R.layout.home_divider_layout)
    )

    class SettingsViewHolder(
        parent: ViewGroup,
        listener: HomeItemAdapter.HomeItemListener
    ) : HomeItemViewHolder<HomeItemModel.SettingsModel>(
        AdapterUtils.inflate(parent, R.layout.home_settings_item_layout)
    ) {
        init {
            view.setOnClickListener { listener.onSettingsClicked() }
        }
    }
}