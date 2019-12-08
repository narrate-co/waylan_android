package space.narrate.waylan.core.data.wordset

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.format.DateTimeFormatter

/**
 * Type converters for unsupported types in Room objects
 */
object WordsetTypeConverters {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val gson = ThreeTenGsonAdapter.registerOffsetDateTime(GsonBuilder()).create()

    @TypeConverter
    @JvmStatic
    fun toExampleList(value: String): List<Example> {
        val type = object : TypeToken<List<Example>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromExampleList(value: List<Example>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toSynonymList(value: String): List<Synonym> {
        val type = object : TypeToken<List<Synonym>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromSynonymList(value: List<Synonym>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toLabelList(value: String): List<Label> {
        val type = object : TypeToken<List<Label>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromLabelList(value: List<Label>): String {
        return gson.toJson(value)
    }
}

