package space.narrate.waylan.core.details

import android.view.ViewGroup

/**
 * Defines a class which is able to provide a ViewHolder to be displayed by the details
 * screen.
 */
interface DetailItemProvider {
    val itemType: DetailItemType
    fun createViewHolder(parent: ViewGroup, listener: DetailAdapterListener): DetailItemViewHolder
}