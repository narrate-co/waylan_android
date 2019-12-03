package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import space.narrate.waylan.merriamwebster_thesaurus.BuildConfig
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry
import kotlin.coroutines.CoroutineContext

/**
 * A data store which is able to retrieve remote Merriam-Webster thesaurus entries and convert
 * those responses to local objects to be used in Waylan.
 */
class MerriamWebsterThesaurusStore(
    private val merriamWebsterThesaurusService: MerriamWebsterThesaurusService
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getWord(word: String): LiveData<List<ThesaurusEntry>> {
        val data = MutableLiveData<List<ThesaurusEntry>>()
        merriamWebsterThesaurusService.getWord(word, DEV_KEY).enqueue(
            object : Callback<List<RemoteThesaurusEntry>> {

                override fun onResponse(
                    call: Call<List<RemoteThesaurusEntry>>,
                    response: Response<List<RemoteThesaurusEntry>>
                ) {
                    // TODO: Pull out toLocalObject extension function.
                    data.value = response.body()?.map { entry ->
                        ThesaurusEntry(
                            entry.meta.id,
                            entry.hwi.hw,
                            entry.meta.src,
                            entry.meta.stems,
                            entry.meta.offensive,
                            entry.fl,
                            entry.shortdef,
                            entry.def.entries.map { it.syn_list }.flatten().flatten().map { it.wd },
                            entry.def.entries.map { it.rel_list }.flatten().flatten().map { it.wd },
                            entry.def.entries.map { it.near_list }.flatten().flatten().map { it.wd },
                            entry.def.entries.map { it.ant_list }.flatten().flatten().map { it.wd }
                        )
                    }
                }

                override fun onFailure(call: Call<List<RemoteThesaurusEntry>>, t: Throwable) {
                    // Possibly handle failure
                    t.printStackTrace()
                }
            })


        return data
    }

    companion object {
        private const val DEV_KEY = BuildConfig.MERRIAM_WEBSTER_THESAURUS_KEY
    }
}