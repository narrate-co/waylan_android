package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.util.LiveDataUtils

/**
 * A data provider which knows how to fetch a [WaylanDefinitionsModel] to be displayed by the
 * details screen.
 */
class WaylanDefinitionsDetailDataProvider(
    private val wordRepository: WordRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
      // TODO: Return a list of user definitions.
      return LiveDataUtils.empty()
    }
}