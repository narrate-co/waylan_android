package space.narrate.waylan.android.data.disk

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import space.narrate.waylan.android.data.disk.mw.MwDefinition
import space.narrate.waylan.android.data.disk.mw.Sound
import space.narrate.waylan.android.data.disk.mw.Uro
import space.narrate.waylan.android.data.disk.wordset.Example
import space.narrate.waylan.android.data.disk.wordset.Label
import space.narrate.waylan.android.data.disk.wordset.Synonym
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Type converters for unsupported types in Room objects
 */
object RoomTypeConverters {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val gson = ThreeTenGsonAdapter.registerOffsetDateTime(GsonBuilder()).create()

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String): OffsetDateTime {
        return formatter.parse(value, OffsetDateTime::from)
    }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(value: OffsetDateTime): String {
        return value.format(formatter)
    }

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
    fun toStringHashSet(value: String): HashSet<String> {
        val type = object : TypeToken<HashSet<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringHashSet(value: HashSet<String>): String {
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
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>): String {
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

