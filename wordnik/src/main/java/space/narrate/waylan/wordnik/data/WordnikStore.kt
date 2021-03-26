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
import space.narrate.waylan.wordnik.data.local.AudioEntry
import space.narrate.waylan.wordnik.data.local.Definition
import space.narrate.waylan.wordnik.data.local.DefinitionEntry
import space.narrate.waylan.wordnik.data.local.Example
import space.narrate.waylan.wordnik.data.local.ExampleEntry
import space.narrate.waylan.wordnik.data.local.FrequencyEntry
import space.narrate.waylan.wordnik.data.local.HyphenationEntry
import space.narrate.waylan.wordnik.data.local.PronunciationEntry
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
        try {
          val response = wordnikService.getDefinitions(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            // Save to cache
            val entry = DefinitionEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // Retrofit or Okhttp threw an an error and it should be handled
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception", e)
        }
      }
    }

    return wordnikDao.getDefinitionEntry(word).filterNotNull().distinctUntilChanged()
  }

  @ExperimentalCoroutinesApi
  fun getExamples(word: String): Flow<ExampleEntry> {

    launch {
      val examples = wordnikDao.getExampleEntryImmediate(word)
      if (examples == null) {
        try {
          val response = wordnikService.getExamples(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            // Save to cache
            val entry = ExampleEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception", e)
        }
      }
    }

    return wordnikDao.getExampleEntry(word).filterNotNull().distinctUntilChanged()
  }

  @ExperimentalCoroutinesApi
  fun getAudio(word: String): Flow<AudioEntry> {

    launch {
      val audios = wordnikDao.getAudioEntryImmediate(word)
      // The audio urls from Wordnik expire quickly and should be re-queried when expired.
      if (audios == null || hasExpiredFileUrls(audios)) {
        try {
          val response = wordnikService.getAudio(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            val entry = AudioEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception: $e")
        }
      }
    }

    return wordnikDao.getAudioEntry(word).filterNotNull().distinctUntilChanged()
  }

  private fun hasExpiredFileUrls(entry: AudioEntry): Boolean {
    val earliestDate = entry.audios
      .map {
        // Get the unix timestamp from the url param "Expires=
        it.fileUrl.split("Expires=")[1].split("&")[0].toLong()
      }
      .minOrNull() ?: return true

    return earliestDate < System.currentTimeMillis()
  }

  @ExperimentalCoroutinesApi
  fun getFrequency(word: String): Flow<FrequencyEntry> {

    launch {
      val frequency = wordnikDao.getFrequencyEntryImmediate(word)
      if (frequency == null) {
        try {
          val response = wordnikService.getFrequency(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            val entry = FrequencyEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception: $e")
        }
      }
    }

    return wordnikDao.getFrequencyEntry(word).filterNotNull().distinctUntilChanged()
  }

  @ExperimentalCoroutinesApi
  fun getHyphenation(word: String): Flow<HyphenationEntry> {

    launch {
      val hyphenation = wordnikDao.getHyphenationEntryImmediate(word)
      if (hyphenation == null) {
        try {
          val response = wordnikService.getHyphenation(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            val entry = HyphenationEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception: $e")
        }
      }
    }

    return wordnikDao.getHyphenationEntry(word).filterNotNull().distinctUntilChanged()
  }

  @ExperimentalCoroutinesApi
  fun getPronunciation(word: String): Flow<PronunciationEntry> {

    launch {
      val pronunciation = wordnikDao.getPronunciationEntryImmediate(word)
      if (pronunciation == null) {
        try {
          val response = wordnikService.getPronunciation(word, BuildConfig.WORDNIK_KEY)
          if (response.isSuccessful && response.body() != null) {
            val entry = PronunciationEntry.fromRemote(word, response.body())
            wordnikDao.insert(entry)
          } else {
            // TODO: Handle error
            Log.e("WordnikStore", response.errorBody().toString())
          }
        } catch (e: Exception) {
          // TODO: Handle error
          Log.e("WordnikStore", "Retrofit/Okhttp exception: $e")
        }
      }
    }

    return wordnikDao.getPronunciationEntry(word).filterNotNull().distinctUntilChanged()
  }
}