package space.narrate.waylan.merriamwebster.data

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import java.lang.Exception
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import space.narrate.waylan.merriamwebster.BuildConfig
import space.narrate.waylan.core.util.contentEquals
import space.narrate.waylan.core.util.hasElapsedMoreThan
import space.narrate.waylan.merriamwebster.data.local.MwDao
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups
import space.narrate.waylan.merriamwebster.data.remote.EntryList
import space.narrate.waylan.merriamwebster.data.remote.EntryUtils
import space.narrate.waylan.merriamwebster.data.remote.MerriamWebsterService
import space.narrate.waylan.merriamwebster.data.remote.synthesizedRelatedWords
import space.narrate.waylan.merriamwebster.data.remote.synthesizedSuggestions
import space.narrate.waylan.merriamwebster.data.remote.toDbMwDefinitions
import space.narrate.waylan.merriamwebster.data.remote.toDbMwWord
import kotlin.coroutines.CoroutineContext

/**
 * The top-most store for access to Merriam-Webster data. This class abstracts details of where
 * Merriam-Webster data comes from by itself handling the aggregation of remote
 * [MerriamWebsterService] data and local [mwDao] data.
 *
 * Clients should not directly interact with this class. Use [WordRepository].
 */
class MerriamWebsterStore(
    private val merriamWebsterService: MerriamWebsterService,
    private val mwDao: MwDao,
    private val ioDispatcher: CoroutineDispatcher
) : CoroutineScope by CoroutineScope(ioDispatcher) {

    /**
     * Immediately returns a LiveData object observing the local Merriam-Webster Room db
     * for [word]. If such an [MwWord] exists, that stored value is returned immediately. Meanwhile,
     * a task is launched to fetch new data from the Merriam-Webster API. On response, the old
     * [MwWord]s and [Definition]s are deleted, the new API data is inserted and the previously
     * returned LiveData receives updates for the relevant changes.
     *
     * @param word The word to query for (as it appears in the dictionary)
     */
    fun getWordAndDefinitions(word: String): LiveData<List<MwWordAndDefinitionGroups>> {

        // Asynchronously get the word from the mw service if it either does not contain any
        // definitions (ie. has never been fetched) or the last time it was fetched was long
        // enough ago to be considered expired and qualifies for a refresh
        launch {
            val definitions = mwDao.getDefinitions(word)
            if (definitions.isNullOrEmpty() || definitions.any { it.lastFetch.isNotFresh() }) {
                try {
                    merriamWebsterService
                        .getWord(word, DEV_KEY)
                        .enqueue(getMerriamWebsterApiWordCallback(word))

                } catch (e: Exception) {
                    // TODO: Handle error
                    Log.e("MerriamWebsterStore", "Retrofit/Okhttp exception", e)
                }
            }
        }

        //TODO possibly create a "SuspendableLiveData" object that can suspend updates to observers
        //while db data is updated. This would avoid the situation of data being present in
        //the db which is returned and then deleting it and receiving a null update and then
        //inserting it and again receiving new (and often times the same) data.

        //return a live data observing the db which will update once the service returns and saved
        //the word to the db
        return mwDao.getWordAndDefinitions(word)
    }

    private fun OffsetDateTime.isNotFresh() = hasElapsedMoreThan(ChronoUnit.DAYS, 7L)

    /**
     * On successful API retrievals, insert (or update by deleting all and then inserting) the
     * response into [MwDao].
     *
     * This turns [MwDao] into an offline cache for the Merriam-Webster API by only ever having
     * clients observe the Merriam-Webster Room Database instead of ever receiving raw [EntryList]
     * data.
     */
    private fun getMerriamWebsterApiWordCallback(word: String) = object : Callback<EntryList> {
        override fun onFailure(call: Call<EntryList>, t: Throwable) {
            // TODO: Handle failure.
            t.printStackTrace()
        }

        override fun onResponse(call: Call<EntryList>, response: Response<EntryList>) {
            //Save to db
            response.body()?.let { entryList ->
                launch {
                    // Delete all existing definition data to keep data fresh
                    entryList.entries.forEach {
                        mwDao.deleteDefinitions(it.word)
                    }

                    // Insert or replace all words
                    entryList.entries.forEach {
                        val w = it.toDbMwWord(
                                entryList.synthesizedRelatedWords,
                                entryList.synthesizedSuggestions
                        )
                        mwDao.insert(w)
                    }

                    // Insert (or reinsert) all definitions
                    entryList.entries.forEach {
                        val definitions = it.toDbMwDefinitions
                        mwDao.insertAll(*definitions.toTypedArray())
                    }

                    // If a response's "entry" is empty, the response often contains a
                    // suggestions list. We should insert this so observers receive updates
                    // to this word and can at least display possible alternative searches
                    val shouldInsertSuggestions =
                            (entryList.entries.isEmpty() && entryList.suggestions.isNotEmpty())
                                    || !entryList.entries.map { it.word }.contains(word)

                    // If empty, in order to return results, add a placeholder word with suggestions
                    if (shouldInsertSuggestions) {
                        val suggestions = entryList.synthesizedSuggestions.filterNot { it == word }

                        val existingMwWord = mwDao.getWord(word)

                        if (existingMwWord != null
                                && !existingMwWord.suggestions.contentEquals(suggestions)) {
                            //this is already present in the db. Update just it's suggestions
                            existingMwWord.suggestions = suggestions
                            mwDao.insert(existingMwWord)
                        } else if (existingMwWord == null) {
                            //this is not already in the db. Add it
                            val newWord = EntryUtils.toDbMwSuggestionWord(word, suggestions)
                            mwDao.insert(newWord)
                        }
                    }
                }
            }
        }
    }

    companion object {
        @VisibleForTesting
        private const val DEV_KEY = BuildConfig.MERRIAM_WEBSTER_KEY
    }
}

