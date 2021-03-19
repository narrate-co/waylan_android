package space.narrate.waylan.americanheritage.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import space.narrate.waylan.wordnik.data.WordnikStore
import space.narrate.waylan.wordnik.data.local.Definition

private const val AMERICAN_HERITAGE_SOURCE_DICTIONARY_TAG = "ahd-5"

/**
 * A repository which knows how to query for American Heritage data.
 */
class AmericanHeritageRepository(
  private val wordnikStore: WordnikStore
) {

  @ExperimentalCoroutinesApi
  fun getDefinitions(word: String): Flow<List<Definition>> {
    return wordnikStore.getDefinitions(word)
      .map { entry ->
        entry.definitions
          .filter { it.sourceDictionary ==  AMERICAN_HERITAGE_SOURCE_DICTIONARY_TAG }
          .toList()
      }
  }
}