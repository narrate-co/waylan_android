package space.narrate.waylan.core.details

import space.narrate.waylan.core.merriamwebster.MerriamWebsterCardListener
import space.narrate.waylan.core.merriamwebster_thesaurus.MerriamWebsterThesaurusCardListener

/**
 * An interface which the details screen uses to delegate user actions back to each
 * DetailDataItem's owning module.
 */
interface DetailAdapterListener : MerriamWebsterCardListener, MerriamWebsterThesaurusCardListener {
    fun onSynonymChipClicked(synonym: String)
}