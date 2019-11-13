package space.narrate.waylan.merriamwebster_thesaurus.data.remote

import com.google.gson.internal.LinkedHashTreeMap
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

data class ThesaurusEntry(
    // Metadata about an entry
    val meta: Meta,
    // Headword information
    val hwi: Hwi,
    // Functional label (such as 'noun' or 'adjective')
    val fl: String,
    val def: List<Def>,
    val shortdef: List<String>
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

//@JsonClass(generateAdapter = true)
//data class Target(
//    val tuuid: String,
//    val tsrc: String
//)

/**
 * Headword Information. The word being defined in a dictionary entry.
 */
data class Hwi(
    // The word being defined
    val hw: String
    // Other fields are available but not included here.
)

data class Def(
    val entries: List<Entry>
)

//data class DefJson(
//    val sseq: List<List<List<Any>>>
//)

class DefinitionAdapter : JsonAdapter<Def>() {
    private val moshi = Moshi.Builder().build()

    override fun fromJson(reader: JsonReader): Def? {
        reader.obj
    }

    override fun toJson(writer: JsonWriter, value: Def?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

//class DefAdapter {
//    @FromJson
//    fun fromAnyList(defJson: DefJson): Def {
//        defJson.sseq.flatten().flatten()
//            .filterIsInstance<LinkedHashTreeMap<String, *>>()
//            .mapNotNull {
//                Entry(
//                    it["dt"],
//                    it
//                )
//            }
//    }
//}



data class Entry(
    val dt: List<List<Any>>,
    val synList: List<List<Wd>>,
    val relList: List<List<Wd>>,
    val nearList: List<List<Wd>>,
    val antList: List<List<Wd>>
)

data class Wd(
    val wd: String,
    val wvrs: List<Wvr>
)

data class Wvr(
    val wv1: String,
    val wva: String
)