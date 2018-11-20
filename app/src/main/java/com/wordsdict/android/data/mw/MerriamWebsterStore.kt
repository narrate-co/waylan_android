package com.wordsdict.android.data.mw

import android.util.Log
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.disk.mw.MwDao
import com.wordsdict.android.data.disk.mw.WordAndDefinitions
import com.wordsdict.android.util.contentEquals
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    fun getWord(word: String): LiveData<List<WordAndDefinitions>> {
        //asynchronously get the word from the mw service
        launch {
            merriamWebsterService.getWord(word, DEV_KEY).enqueue(getMerriamWebsterApiWordCallback(word))
        }

        //return a live data observing the db which will update once the service returns and saved
        //the word to the db
        return mwDao.getWordAndDefinitions(word)
    }

    private fun getMerriamWebsterApiWordCallback(word: String) = object : Callback<EntryList> {
        override fun onFailure(call: Call<EntryList>?, t: Throwable?) {
            Log.e(TAG, "mwApiWordCallback on Failure = $t")
            analyticsRepository.logMerriamWebsterParseErrorEvent(word, t.toString())
        }
        override fun onResponse(call: Call<EntryList>?, response: Response<EntryList>?) {
            //Save to db
            response?.body()?.let { entryList ->
                launch {


                    //Input all definitions and words if they exist - keep data fresh
                    entryList.entries.forEach {

                        mwDao.deleteDefinitions(it.word)

                    }

                    val relatedWords = entryList.entries.map { it.word }

                    entryList.entries.forEach {
                        val word = it.toDbMwWord(relatedWords, entryList.suggestions)
                        mwDao.insert(word)
                    }

                    entryList.entries.forEach {

                        val definitions = it.toDbMwDefinitions
                        mwDao.insertAll(*definitions.toTypedArray())
                    }

                    //if empty, in order to return results, add a placeholder word with suggestions
                    if (entryList.entries.isEmpty() && entryList.suggestions.isNotEmpty()) {
                        val existingMwWord = mwDao.getWord(word)
                        if (existingMwWord != null && existingMwWord.suggestions.contentEquals(entryList.suggestions)) {
                            //this is already present in the db. Update just it's suggestions
                            existingMwWord.suggestions = entryList.suggestions
                            mwDao.insert(existingMwWord)
                        } else if (existingMwWord == null) {
                            //this is not already in the db. Add it
                            val newWord = entryList.toSuggestionWord(word)
                            mwDao.insert(newWord)
                        }
                    }
                }
            }
        }
    }
}

