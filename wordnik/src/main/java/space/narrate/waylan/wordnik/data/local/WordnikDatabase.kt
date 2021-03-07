package space.narrate.waylan.wordnik.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import space.narrate.waylan.core.util.RoomTypeConverters

/**
 * Database which acts as an offline cache for Wordnik data. When a new word is queried,
 * the word is retrieved from thw Wordnik API and placed into this database before being fed
 * to the UI. Subsequent lookups will pull directly from this db instead of calling the API.
 */
@Database(
    entities = [
        DefinitionEntry::class,
        ExampleEntry::class,
        AudioEntry::class,
        FrequencyEntry::class,
        HyphenationEntry::class,
        PronunciationEntry::class
    ],
    version = 2
)
@TypeConverters(RoomTypeConverters::class, WordnikTypeConverters::class)
abstract class WordnikDatabase : RoomDatabase() {

    abstract fun wordnikDao(): WordnikDao

    companion object {

        @VisibleForTesting
        private const val DATABASE_NAME = "wordnik-db"

        @Volatile
        private var instance: WordnikDatabase? = null


        fun getInstance(context: Context): WordnikDatabase =
            instance ?: synchronized(this) {
                instance ?: (buildDatabase(context.applicationContext, DATABASE_NAME)).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context, dbName: String): WordnikDatabase {
            return Room
                .databaseBuilder(context, WordnikDatabase::class.java, dbName)
                .addMigrations(MIGRATION_1_to_2)
                .build()
        }

        private val MIGRATION_1_to_2 = object: Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // do nothing.
                // version 1 had: wordnik_definitions
                // version 2 added: wordnik_examples,audio,frequency,hyphenation,pronunciation
            }
        }

    }
}

