package space.narrate.waylan.merriamwebster.data

import androidx.lifecycle.LiveData
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups

/**
 * A repository which knows how to query for Merriam-Webster data.
 */
class MerriamWebsterRepository(
    private val merriamWebsterStore: MerriamWebsterStore
) {
    fun getMerriamWebsterWord(word: String): LiveData<List<MwWordAndDefinitionGroups>> {
        return merriamWebsterStore.getWordAndDefinitions(word)
    }
}