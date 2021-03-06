package space.narrate.waylan.americanheritage.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import space.narrate.waylan.americanheritage.data.AmericanHeritageRepository
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.util.MergedLiveData

class AmericanHeritageDetailDataProvider(
  private val americanHeritageRepository: AmericanHeritageRepository,
  private val userRepository: UserRepository
) : DetailDataProvider {
  override fun loadWord(word: String): LiveData<DetailItemModel> {
    return MergedLiveData(
      americanHeritageRepository.getDefinitions(word).asLiveData(),
      userRepository.getUserAddOnLive(AddOn.AMERICAN_HERITAGE)
    ) { definitions, userAddOn -> AmericanHeritageModel(definitions, userAddOn) }
  }
}