package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry

/**
 * The remote representation of a response from the Merriam-Webster thesaurus API.
 */
data class RemoteThesaurusEntry(
    // Metadata about an entry
    val meta: Meta,
    // Headword information
    val hwi: Hwi,
    // Functional label (such as 'noun' or 'adjective')
    val fl: String,
    val def: Def,
    val shortdef: List<String>
)

val RemoteThesaurusEntry.toLocalThesaurusEntry: ThesaurusEntry
    get() = ThesaurusEntry(
        meta.id,
        hwi.hw,
        meta.src,
        meta.stems,
        meta.offensive,
        fl,
        shortdef,
        def.entries.map { it.syn_list }.flatten().flatten().map { it.wd },
        def.entries.map { it.rel_list }.flatten().flatten().map { it.wd },
        def.entries.map { it.near_list }.flatten().flatten().map { it.wd },
        def.entries.map { it.ant_list }.flatten().flatten().map { it.wd },
        OffsetDateTime.now()
    )

/**
 * Metadata about an entry.
 */
data class Meta(
    // Unique entry identifier
    val id: String,
    val uuid: String,
    // Source data set for the entry
    val src: String,
    // The section the entry belongs to in the dictionary (ie. 'alpha', 'biog', 'geog').
    val section: String,
    val stems: List<String>,
    val syns: List<List<String>>,
    val ants: List<List<String>>,
    val offensive: Boolean
)

/**
 * Headword Information. The word being defined in a dictionary entry.
 */
data class Hwi(
    // The word being defined
    val hw: String
    // Other fields are available but not included here.
)

data class Def(
    val entries: List<Entry> = emptyList()
)

data class Entry(
    val sn: String = "",
    val dt: Any = Any(),
    val syn_list: List<List<Wd>> = emptyList(),
    val rel_list: List<List<Wd>> = emptyList(),
    val near_list: List<List<Wd>> = emptyList(),
    val ant_list: List<List<Wd>> = emptyList()
)

data class Wd(
    val wd: String,
    val wvrs: List<Wvr> = emptyList()
)

data class Wvr(
    val wv1: String?,
    val wva: String
)

class EntryAdapter {
    // TODO: Find a cleaner way to parse the 'def' section of the response.
    @FromJson
    fun fromJson(defObject: Any): Def {
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            // ArrayList<Array[sense, Entry]>
            val listOfEntryParents = ((defObject as ArrayList<Map<*, *>>)[0]["sseq"]
                as ArrayList<ArrayList<*>>).map { it.first() } as ArrayList<ArrayList<*>>

            val entries = listOfEntryParents.mapNotNull {
                moshi.adapter(Entry::class.java).fromJsonValue(it[1])
            }

            return Def(entries)
        } catch (e: Exception) {
            e.printStackTrace()
            Def()
        }
    }
}