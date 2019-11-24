package space.narrate.waylan.merriamwebster_thesaurus.data

import androidx.lifecycle.LiveData
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusStore

class MerriamWebsterThesaurusRepository(
    private val merriamWebsterThesaurusStore: MerriamWebsterThesaurusStore
) {
    fun getMerriamWebsterThesaurusWord(word: String): LiveData<List<ThesaurusEntry>> {
        return merriamWebsterThesaurusStore.getWord(word)
    }
}