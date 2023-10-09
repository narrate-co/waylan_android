package space.narrate.waylan.wordset

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.util.notNullTransform

class WordsetDetailDataProvider(
  private val wordRepository: WordRepository
) : DetailDataProvider {
  override fun loadWord(word: String): LiveData<DetailItemModel> {
    return wordRepository.getWordsetWordAndMeanings(word)
      .notNullTransform()
      .map {
        WordsetModel(
          it,
          it.meanings.map { m -> m.examples }.flatten()
        )
      }
  }
}