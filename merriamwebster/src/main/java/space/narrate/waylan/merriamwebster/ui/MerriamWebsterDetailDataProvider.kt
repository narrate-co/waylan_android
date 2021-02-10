package space.narrate.waylan.merriamwebster.ui

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.util.MergedLiveData
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.merriamwebster.data.MerriamWebsterRepository

/**
 * A [DetailDataProvider] which knows how to fetch a [MerriamWebsterModel] to be displayed by
 * the details screen.
 */
class MerriamWebsterDetailDataProvider(
    private val merriamWebsterRepository: MerriamWebsterRepository,
    private val userRepository: UserRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
        return MergedLiveData(
            merriamWebsterRepository.getMerriamWebsterWord(word),
            userRepository.getUserAddOnLive(AddOn.MERRIAM_WEBSTER)
        ) { mw, userAddOn -> MerriamWebsterModel(mw, userAddOn) }
    }
}