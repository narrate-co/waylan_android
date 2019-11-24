package space.narrate.waylan.merriamwebster_thesaurus.ui

import androidx.lifecycle.LiveData
import space.narrate.waylan.android.data.repository.UserRepository
import space.narrate.waylan.android.util.MergedLiveData
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.merriamwebster_thesaurus.data.MerriamWebsterThesaurusRepository

class MerriamWebsterThesaurusDetailDataProvider(
    private val repository: MerriamWebsterThesaurusRepository,
    private val userRepository: UserRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
        return MergedLiveData(
            repository.getMerriamWebsterThesaurusWord(word),
            userRepository.user
        ) { mwt, user -> MerriamWebsterThesaurusModel(mwt, user) }
    }
}