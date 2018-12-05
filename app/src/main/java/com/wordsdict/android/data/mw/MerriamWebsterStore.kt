package com.wordsdict.android.data.mw

import android.util.Log
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.disk.mw.*
import com.wordsdict.android.util.contentEquals
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        private val analyticsRepository: AnalyticsRepository
) {

    companion object {
        private const val TAG = "MerriamWebsterStore"

        //TODO obfuscate
        private const val DEV_KEY = "d0eece12-48a6-47e3-bcbe-6a4eec0ed3c2"
    }

    /**
     * Immediately returns a LiveData object observing the local Merriam-Webster Room db
     * for [word]. If such an [Word] exists, that stored value is returned immediately. Meanwhile,
     * a task is launched to fetch new data from the Merriam-Webster API. On response, the old
     * [Word]s and [Definition]s are deleted, the new API data is inserted and the previously
     * returned LiveData receives updates for the relevant changes.
     *
     * @param word The word to query for (as it appears in the dictionary)
     */
    fun getWordAndDefinitions(word: String): LiveData<List<WordAndDefinitions>> {

        //TODO only luanch this if the word needs to be updated.
        //asynchronously get the word from the mw service
        launch {
            merriamWebsterService
                    .getWord(word, DEV_KEY)
                    .enqueue(getMerriamWebsterApiWordCallback(word))
        }

        //TODO possible create a "SuspendableLiveData" object that can suspend updates to observers
        //TODO while db data is updated. This would avoid the situation of data being present in
        //TODO the db which is returned and then deleting it and receiving a null update and then
        //TODO inserting it and again receiving new (and often times the same) data.
        //return a live data observing the db which will update once the service returns and saved
        //the word to the db
        return mwDao.getWordAndDefinitions(word)
    }


    /**
     * On successful API retrievals, insert (or update by deleting all and then inserting) the
     * response into [MwDao].
     *
     * This turns [MwDao] into an offline cache for the Merriam-Webster API by only ever having
     * clients observe the Merriam-Webster Room Database instead of ever receiving raw [EntryList]
     * data.
     */
    private fun getMerriamWebsterApiWordCallback(word: String) = object : Callback<EntryList> {
        override fun onFailure(call: Call<EntryList>?, t: Throwable?) {
            Log.e(TAG, "mwApiWordCallback on Failure = $t")
            Crashlytics.logException(t)
        }
        override fun onResponse(call: Call<EntryList>?, response: Response<EntryList>?) {
            //Save to db
            response?.body()?.let { entryList ->
                launch {


                    // Delete all existing definition data to keep data fresh
                    entryList.entries.forEach {
                        mwDao.deleteDefinitions(it.word)
                    }

                    // Insert or replace all words
                    entryList.entries.forEach {
                        val word = it.toDbMwWord(
                                entryList.synthesizedRelatedWords,
                                entryList.synthesizedSuggestions
                        )
                        mwDao.insert(word)
                    }

                    // Insert (or reinsert) all definitions
                    entryList.entries.forEach {
                        val definitions = it.toDbMwDefinitions
                        mwDao.insertAll(*definitions.toTypedArray())
                    }

                    // If a response's "entries" is empty, the response often contains a
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
                            val newWord = toDbMwSuggestionWord(word, suggestions)
                            mwDao.insert(newWord)
                        }
                    }
                }
            }
        }
    }
}

