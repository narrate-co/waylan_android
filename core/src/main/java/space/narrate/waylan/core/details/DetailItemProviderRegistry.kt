package space.narrate.waylan.core.details

import android.view.ViewGroup

/**
 * A registry with which modules can register their DetailItemProvider implementations to have the
 * details screen show their ViewHolder for their given [DetailItemModel].
 */
class DetailItemProviderRegistry {

    private val providers: MutableSet<DetailItemProvider> = mutableSetOf()

    fun addProviders(vararg providers: DetailItemProvider) {
        this.providers.addAll(providers)
    }

    fun createViewHolderFor(
        itemType: DetailItemType,
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder? {
        return providers
            .firstOrNull { it.itemType == itemType }
            ?.createViewHolder(parent, listener)
    }
}