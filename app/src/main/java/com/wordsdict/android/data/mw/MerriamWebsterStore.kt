package com.wordsdict.android.data.mw

import android.util.Log
import androidx.lifecycle.LiveData
import com.crashlytics.android.Crashlytics
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.disk.mw.Definition
import com.wordsdict.android.data.disk.mw.MwDao
import com.wordsdict.android.data.disk.mw.Word
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

    fun getWordAndDefinitions(word: String): LiveData<List<WordAndDefinitions>> {
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
            Crashlytics.logException(t)
        }
        override fun onResponse(call: Call<EntryList>?, response: Response<EntryList>?) {
            //Save to db
            response?.body()?.let { entryList ->
                launch {


                    //Input all definitions and words if they exist - keep data fresh
                    entryList.entries.forEach {

                        mwDao.deleteDefinitions(it.word)

                    }

                    println("$TAG::onResponse")

                    val relatedWords = entryList.entries.map { it.word }.distinct()

                    entryList.entries.forEach {
                        val word = it.toDbMwWord(relatedWords, entryList.synthesizedSuggestions)
                        println("$TAG::inserting word id = ${word.id}")
                        val inserted = mwDao.insert(word)
                        println("$TAG::inserted word = $inserted")
                    }

                    entryList.entries.forEach {

                        val definitions = it.toDbMwDefinitions
                        definitions.forEach {def ->
                            println("$TAG::inserting definition. parentId = ${def.parentId}, def = ${def.definitions}")
                        }
                        val inserted = mwDao.insertAll(*definitions.toTypedArray())
                        inserted.forEach { long ->
                            println("$TAG::inserted definition = $long")
                        }
                    }

                    val shouldInsertSuggestions = (entryList.entries.isEmpty() && entryList.suggestions.isNotEmpty()) || !entryList.entries.map { it.word }.contains(word)
                    //if empty, in order to return results, add a placeholder word with suggestions
                    if (shouldInsertSuggestions) {
                        val suggs = entryList.synthesizedSuggestions.filterNot { it == word }


                        val existingMwWord = mwDao.getWord(word)
                        if (existingMwWord != null && !existingMwWord.suggestions.contentEquals(suggs)) {
                            //this is already present in the db. Update just it's suggestions
                            existingMwWord.suggestions = suggs
                            mwDao.insert(existingMwWord)
                        } else if (existingMwWord == null) {
                            //this is not already in the db. Add it
//                            val newWord = entryList.toSuggestionWord(word, suggs)
                            val newWord = getNewSuggestionWord(word, suggs)
                            mwDao.insert(newWord)
                        }
                    }
                }
            }
        }
    }
}

