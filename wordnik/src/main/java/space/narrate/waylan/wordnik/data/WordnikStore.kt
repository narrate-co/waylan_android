package space.narrate.waylan.wordnik.data

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.wordnik.BuildConfig
import space.narrate.waylan.wordnik.data.local.Definition
import space.narrate.waylan.wordnik.data.local.DefinitionEntry
import space.narrate.waylan.wordnik.data.local.WordnikDao
import space.narrate.waylan.wordnik.data.remote.WordnikService

class WordnikStore(
  private val wordnikService: WordnikService,
  private val wordnikDao: WordnikDao,
  private val ioDispatcher: CoroutineDispatcher
) : CoroutineScope by CoroutineScope(ioDispatcher) {

  @ExperimentalCoroutinesApi
  fun getDefinitions(word: String): Flow<DefinitionEntry> {

    // Check if this word is in the database. If it is, skip calling the Wordnik API. If it
    // isn't, call the Wordnik API and cache the results in the local db.
    launch {
      val definitions = wordnikDao.getDefinitionEntryImmediate(word)
      if (definitions == null) {
        val response = wordnikService.getDefinitions(word, BuildConfig.WORDNIK_KEY)
        if (response.isSuccessful && response.body() != null) {
          // Save to cache
          val entry = DefinitionEntry(
            word,
            word,
            response.body()?.map {
              Definition(
                it.id ?: "",
                it.partOfSpeech ?: "",
                it.attributionText ?: "",
                it.sourceDictionary ?: "",
                it.text ?: "",
                it.sequence ?: "",
                it.score ?: -1,
                it.labels ?: emptyList(),
                it.citations ?: emptyList(),
                it.word ?: word,
                it.relatedWords ?: emptyList(),
                it.exampleUses ?: emptyList(),
                it.textProns ?: emptyList(),
                it.notes ?: emptyList(),
                it.attributionUrl ?: "",
                it.wordnikUrl ?: ""
              )
            } ?: emptyList(),
            OffsetDateTime.now()
          )
          wordnikDao.insert(entry)
        } else {
          // TODO: Handle error
          Log.e("WordnikStore", response.errorBody().toString())
        }
      }
    }

    return wordnikDao.getDefinitionEntry(word).filterNotNull().distinctUntilChanged()
  }
}