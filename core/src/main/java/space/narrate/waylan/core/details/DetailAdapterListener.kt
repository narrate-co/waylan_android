package space.narrate.waylan.core.details

import space.narrate.waylan.core.merriamwebster.MerriamWebsterCardListener

/**
 * An interface which the details screen uses to delegate user actions back to each
 * DetailDataItem's owning module.
 */
interface DetailAdapterListener : MerriamWebsterCardListener {
    fun onSynonymChipClicked(synonym: String)
}