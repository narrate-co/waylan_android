package space.narrate.waylan.merriamwebster_thesaurus.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import space.narrate.waylan.core.util.hasElapsedMoreThan
import space.narrate.waylan.merriamwebster_thesaurus.BuildConfig
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusDao
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.MerriamWebsterThesaurusService
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.RemoteThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.toLocalThesaurusEntry
import kotlin.coroutines.CoroutineContext

/**
 * A data store which is able to retrieve remote Merriam-Webster thesaurus entries and convert
 * those responses to local objects to be used in Waylan.
 */
class MerriamWebsterThesaurusStore(
    private val merriamWebsterThesaurusService: MerriamWebsterThesaurusService,
    private val thesaurusDao: ThesaurusDao
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun getWord(word: String): LiveData<List<ThesaurusEntry>> {

        launch {
            val entries = thesaurusDao.getWord(word)
            if (entries.isNullOrEmpty() || entries.any { it.lastFetch.isNotFresh() }) {
                merriamWebsterThesaurusService
                    .getWord(word, DEV_KEY)
                    .enqueue(getApiCallback())
            }
        }

        return thesaurusDao.getWordLive(word)
    }

    private fun OffsetDateTime.isNotFresh() = hasElapsedMoreThan(ChronoUnit.DAYS, 7L)

    private fun getApiCallback() = object : Callback<List<RemoteThesaurusEntry>> {
        override fun onFailure(call: Call<List<RemoteThesaurusEntry>>, t: Throwable) {
            // Possibly handle failure
            t.printStackTrace()
        }

        override fun onResponse(
            call: Call<List<RemoteThesaurusEntry>>,
            response: Response<List<RemoteThesaurusEntry>>
        ) {
            response.body()?.let { remoteEntries ->
                launch {
                    val localEntries = remoteEntries.map { it.toLocalThesaurusEntry }
                    thesaurusDao.insertAll(*localEntries.toTypedArray())
                }
            }
        }
    }

    companion object {
        private const val DEV_KEY = BuildConfig.MERRIAM_WEBSTER_THESAURUS_KEY
    }
}