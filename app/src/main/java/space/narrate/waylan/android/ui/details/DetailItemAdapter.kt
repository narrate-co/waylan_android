package space.narrate.waylan.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemProviderRegistry
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.AdapterUtils
import java.lang.IllegalArgumentException

class DetailItemAdapter(
    private val itemProviderRegistry: DetailItemProviderRegistry,
    private val listener: DetailAdapterListener
) : ListAdapter<DetailItemModel, DetailItemViewHolder>(
    AdapterUtils.diffableItemCallback()
) {

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        require(item is DetailItemModel) {
            "The item must be an implementation of DetailItemModel but was ${item::class.java}"
        }

        // TODO: Add property to DetailItemType to avoid using ordinal.
        return item.itemType.ordinal
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailItemViewHolder {
        val itemType = DetailItemType.values()[viewType]
        return itemProviderRegistry.createViewHolderFor(itemType, parent, listener)
            ?: throw IllegalArgumentException(
                "The view holder for $itemType does not have a provider added to " +
                    "this adapter's DetailItemFactory"
            )
    }

    override fun onBindViewHolder(
        holder: DetailItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}