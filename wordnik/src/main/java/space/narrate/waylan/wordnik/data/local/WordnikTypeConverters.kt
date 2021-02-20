package space.narrate.waylan.wordnik.data.local

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.format.DateTimeFormatter

object WordnikTypeConverters {

  private val gson = ThreeTenGsonAdapter.registerOffsetDateTime(GsonBuilder()).create()

  @TypeConverter
  @JvmStatic
  fun toDefinitionList(value: String): List<Definition> {
    val type = object : TypeToken<List<Definition>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromDefinitionList(value: List<Definition>): String {
    return gson.toJson(value)
  }

  @TypeConverter
  @JvmStatic
  fun toExampleList(value: String): List<Example> {
    val type = object : TypeToken<List<Definition>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromExampleList(value: List<Example>): String {
    return gson.toJson(value)
  }
}