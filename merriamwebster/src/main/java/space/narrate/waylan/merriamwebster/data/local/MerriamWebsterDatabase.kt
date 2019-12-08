package space.narrate.waylan.merriamwebster.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.narrate.waylan.core.util.RoomTypeConverters

/**
 * Database which acts as an offline cache for Merriam-Webster data. When a new word is querried,
 * the word is retrieved from thw Merriam-Webster API and placed into this database before being fed
 * to the UI. Subsequent lookups will pull directly from this db instead of calling the API.
 */
@Database(
    entities = [
        MwWord::class,
        MwDefinitionGroup::class
    ],
    version = 1)
@TypeConverters(RoomTypeConverters::class, MerriamWebsterTypeConverters::class)
abstract class MerriamWebsterDatabase: RoomDatabase() {

    abstract fun mwDao(): MwDao

    companion object {

        @VisibleForTesting
        private const val DATABASE_NAME = "merriam-webster-db"

        @Volatile
        private var instance: MerriamWebsterDatabase? = null


        fun getInstance(context: Context): MerriamWebsterDatabase =
            instance ?: synchronized(this) {
                instance ?: (buildDatabase(context.applicationContext, DATABASE_NAME)).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context, dbName: String): MerriamWebsterDatabase {
            return Room
                .databaseBuilder(context, MerriamWebsterDatabase::class.java, dbName)
                .build()
        }

    }
}

