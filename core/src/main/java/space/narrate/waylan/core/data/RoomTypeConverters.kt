package space.narrate.waylan.core.data

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
}

