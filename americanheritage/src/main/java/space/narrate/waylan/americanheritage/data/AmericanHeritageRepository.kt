package space.narrate.waylan.americanheritage.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.narrate.waylan.wordnik.data.WordnikStore
import space.narrate.waylan.wordnik.data.local.Audio
import space.narrate.waylan.wordnik.data.local.Definition
import space.narrate.waylan.wordnik.data.local.Pronunciation

private const val AMERICAN_HERITAGE_SOURCE_DICTIONARY_TAG = "ahd-5"
private const val AMERICAN_HERITAGE_CREATED_BY_TAG = "ahd"

/**
 * A repository which knows how to query for American Heritage data.
 */
@ExperimentalCoroutinesApi
class AmericanHeritageRepository(
  private val wordnikStore: WordnikStore
) {

  fun getDefinitions(word: String): Flow<List<Definition>> {
    return wordnikStore.getDefinitions(word)
      .map { entry ->
        entry.definitions
          .filter { it.sourceDictionary ==  AMERICAN_HERITAGE_SOURCE_DICTIONARY_TAG }
          .toList()
      }
  }

  fun getAudio(word: String): Flow<List<Audio>> {
    return wordnikStore.getAudio(word)
      .map { entry ->
        entry.audios
          .filter { it.createdBy == AMERICAN_HERITAGE_CREATED_BY_TAG }
          .toList()
      }
  }
}