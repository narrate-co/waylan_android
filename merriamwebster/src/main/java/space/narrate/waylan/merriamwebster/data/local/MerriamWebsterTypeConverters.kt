package space.narrate.waylan.merriamwebster.data.local

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.format.DateTimeFormatter

/**
 * Type converters for unsupported types in Room objects
 */
object MerriamWebsterTypeConverters {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val gson = ThreeTenGsonAdapter.registerOffsetDateTime(GsonBuilder()).create()

    @TypeConverter
    @JvmStatic
    fun toUro(value: String): Uro {
        val type = object : TypeToken<Uro>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromUro(value: Uro): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toUroList(value: String): List<Uro> {
        val type = object : TypeToken<List<Uro>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromUroList(value: List<Uro>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toSoundList(value: String): List<Sound> {
        val type = object : TypeToken<List<Sound>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromSoundList(value: List<Sound>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toSound(value: String): Sound {
        val type = object : TypeToken<Sound>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromSound(value: Sound): String {
        return gson.toJson(value)
    }

    @TypeConverter
    @JvmStatic
    fun toOrderedDefinitionItemList(value: String): List<MwDefinition> {
        val type = object : TypeToken<List<MwDefinition>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromOrderedDefinitionItemList(value: List<MwDefinition>): String {
        return gson.toJson(value)
    }
}

