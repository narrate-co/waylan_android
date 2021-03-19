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
  fun toAudioList(value: String): List<Audio> {
    val type = object : TypeToken<List<Audio>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromAudioList(value: List<Audio>): String {
    return gson.toJson(value)
  }

  @TypeConverter
  @JvmStatic
  fun toFrequencyList(value: String): List<Frequency> {
    val type = object : TypeToken<List<Frequency>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromFrequencyList(value: List<Frequency>): String {
    return gson.toJson(value)
  }

  @TypeConverter
  @JvmStatic
  fun toHyphenationList(value: String): List<Hyphenation> {
    val type = object : TypeToken<List<Hyphenation>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromHyphenationList(value: List<Hyphenation>): String {
    return gson.toJson(value)
  }

  @TypeConverter
  @JvmStatic
  fun toPronunciationList(value: String): List<Pronunciation> {
    val type = object : TypeToken<List<Pronunciation>>() {}.type
    return gson.fromJson(value, type)
  }

  @TypeConverter
  @JvmStatic
  fun fromPronunciationList(value: List<Pronunciation>): String {
    return gson.toJson(value)
  }
}