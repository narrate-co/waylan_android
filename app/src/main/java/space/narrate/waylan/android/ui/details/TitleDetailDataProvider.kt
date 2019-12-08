package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import space.narrate.waylan.core.data.repo.WordRepository
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel

/**
 * A data provider which knows how to fetch a [TitleModel], given a word, to be
 * displayed by the details screen.
 */
class TitleDetailDataProvider(
    private val wordRepository: WordRepository
) : DetailDataProvider {

    override fun loadWord(word: String): LiveData<DetailItemModel> {
        return wordRepository.getWordsetWord(word).mapTransform {
            TitleModel(it?.word ?: word)
        }
    }
}