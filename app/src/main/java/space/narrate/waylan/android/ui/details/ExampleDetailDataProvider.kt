package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.data.repo.WordRepository
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.notNullTransform
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel

/**
 * A data provider which knows how to fetch an [ExamplesModel] to be displayed by the
 * details screen
 */
class ExampleDetailDataProvider(
    private val wordRepository: WordRepository
) : DetailDataProvider {
    override fun loadWord(word: String): LiveData<DetailItemModel> {
        return wordRepository.getWordsetWordAndMeanings(word)
            .notNullTransform()
            .mapTransform {
                ExamplesModel(it.meanings.map { m -> m.examples }.flatten())
            }
    }
}