package space.narrate.waylan.americanheritage.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.map
import space.narrate.waylan.americanheritage.data.AmericanHeritageRepository
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel

class AmericanHeritageDetailDataProvider(
  private val americanHeritageRepository: AmericanHeritageRepository
) : DetailDataProvider {
  override fun loadWord(word: String): LiveData<DetailItemModel> {
    return americanHeritageRepository.getDefinitions(word)
      .map { AmericanHeritageModel(it) }
      .asLiveData()
  }
}