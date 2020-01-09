package space.narrate.waylan.core.merriamwebster_thesaurus

import space.narrate.waylan.core.ui.common.AddOnListener

/**
 * An interface which allows clients which display a MerriamWebsterCardViewThesaurus to listen
 * to events which happen on that view when the client does not depend on :merriamwebster_thesaurus.
 */
interface MerriamWebsterThesaurusCardListener : AddOnListener {
    fun onMwThesaurusChipClicked(word: String)
}