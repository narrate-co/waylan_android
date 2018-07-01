package com.words.android.data.mw

import android.util.Log
import androidx.lifecycle.LiveData
import com.words.android.data.disk.mw.MwDao
import com.words.android.data.disk.mw.WordAndDefinitions
import kotlinx.coroutines.experimental.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MerriamWebsterStore(
        private val merriamWebsterService: MerriamWebsterService,
        private val mwDao: MwDao
) {

    companion object {
        private const val TAG = "MerriamWebsterStore"
        //TODO update with real key
        private const val DEV_KEY = "1234"
    }

    fun getWord(word: String): LiveData<WordAndDefinitions> {
        //asynchronously get the word from the mw service
        launch { merriamWebsterService.getWord(word, DEV_KEY).enqueue(mwApiWordCallback) }

        //return a live data observing the db which will update once the service returns and saved
        //the word to the db
        return mwDao.getWordAndDefinitions(word)
    }

    private val mwApiWordCallback = object : Callback<MwApiWord> {
        override fun onFailure(call: Call<MwApiWord>?, t: Throwable?) {
            Log.e(TAG, "mwApiWordCallback on Failure = $t")
        }
        override fun onResponse(call: Call<MwApiWord>?, response: Response<MwApiWord>?) {
            //Save to db
            response?.body()?.let {
                mwDao.insert(it.toDbMwWord)
                mwDao.insertAll(*it.toDbMwDefinitions.toTypedArray())
            }
        }
    }

}

