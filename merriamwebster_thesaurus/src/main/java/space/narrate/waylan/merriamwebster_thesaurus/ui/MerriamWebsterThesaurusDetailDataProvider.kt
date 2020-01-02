package space.narrate.waylan.merriamwebster_thesaurus.ui

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.android.util.MergedLiveData
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.merriamwebster_thesaurus.data.MerriamWebsterThesaurusRepository

/**
 * A [DetailDataProvider] which knows how to fetch a [MerriamWebsterThesaurusModel] to be displayed
 * by the details screen.
 */
class MerriamWebsterThesaurusDetailDataProvider(
    private val repository: MerriamWebsterThesaurusRepository,
    private val userRepository: UserRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
        return MergedLiveData(
            repository.getMerriamWebsterThesaurusWord(word),
            userRepository.getUserAddOnLive(AddOn.MERRIAM_WEBSTER_THESAURUS)
        ) { mwt, userAddOn -> MerriamWebsterThesaurusModel(mwt, userAddOn) }
    }
}