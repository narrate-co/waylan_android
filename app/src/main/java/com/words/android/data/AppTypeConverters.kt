package com.words.android.data

import androidx.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.words.android.data.disk.Example
import com.words.android.data.disk.Label
import com.words.android.data.disk.Synonym
import org.aaronhe.threetengson.ThreeTenGsonAdapter
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter

object AppTypeConverters {

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
    fun toLableList(value: String): List<Label> {
        val type = object : TypeToken<List<Label>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    @JvmStatic
    fun fromLabelList(value: List<Label>): String {
        return gson.toJson(value)
    }
}

