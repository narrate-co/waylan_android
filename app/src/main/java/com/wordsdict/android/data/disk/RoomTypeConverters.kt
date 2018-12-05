package com.wordsdict.android.data.disk

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.wordsdict.android.data.disk.mw.OrderedDefinitionItem
import com.wordsdict.android.data.disk.mw.Sound
import com.wordsdict.android.data.disk.mw.Uro
import com.wordsdict.android.data.disk.wordset.Example
import com.wordsdict.android.data.disk.wordset.Label
import com.wordsdict.android.data.disk.wordset.Synonym
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
    fun toOrderedDefinitionItemList(value: String): List<OrderedDefinitionItem> {
        val type = object : TypeToken<List<OrderedDefinitionItem>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromOrderedDefinitionItemList(value: List<OrderedDefinitionItem>): String {
        return gson.toJson(value)
    }
}

