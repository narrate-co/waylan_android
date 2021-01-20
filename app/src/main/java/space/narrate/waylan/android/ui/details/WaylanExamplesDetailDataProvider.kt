package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.util.toLiveData

/**
 * A data provider which knows how to fetch an [WaylanExamplesModel] to be displayed by the
 * details screen
 */
class WaylanExamplesDetailDataProvider(
    private val wordRepository: WordRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
      // TODO: Return a list of a user's examples.
      return WaylanExamplesModel(emptyList()).toLiveData
    }
}