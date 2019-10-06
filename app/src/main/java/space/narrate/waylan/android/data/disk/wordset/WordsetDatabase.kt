package space.narrate.waylan.android.data.disk.wordset

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import space.narrate.waylan.core.data.RoomTypeConverters
import java.io.File
import java.lang.Exception

/**
 *
 * A [RoomDatabase] that provides the two local data sources - WordSet and previously
 * fetched (cached) Merriam-Webster entry
 *
 * WordSet (<a>https://github.com/wordset/wordset-dictionary</a>) is db that is shipped with
 * Words, pre-populated and copied on first access by [RoomAsset]
 *
 * Merriam-Webster is a db that hold all definitions ever retrieved from the Merriam-Webster API.
 * When searching a word on Words, [WordRepository] immediately returns a LiveData object of mwDao.
 * It then fetches the word from Merriam-Webster and inserts it into [MwDao], which is seen by
 * the previously returned LiveData object. This works as a cache for words that have previously
 * been looked up by immediately returning stored Merriam-Webster words while refreshing the data
 * in the background (and then diffing it with the current displayed data before making any
 * UI changes).
 */
@Database(
    entities = [
        Word::class,
        Meaning::class
    ],
    version = 1)
@TypeConverters(RoomTypeConverters::class, WordsetTypeConverters::class)
abstract class WordsetDatabase: RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun meaningDao(): MeaningDao

    companion object {

        @VisibleForTesting
        private const val DATABASE_NAME = "wordset-db"

        // Manually flip this boolean to run the app and have the database populated from
        // the included json dictionary assets. Extract the resulting .db file and include it in
        // production builds so it can be used to immediately create the database.
        private const val SHOULD_SEED_DATABASE = false

        @Volatile
        private var instance: WordsetDatabase? = null


        fun getInstance(context: Context): WordsetDatabase =
            instance ?: synchronized(this) {
                instance ?: (
                    if (SHOULD_SEED_DATABASE) {
                        seedAndBuildDatabase(context.applicationContext, DATABASE_NAME)
                    }
                    else {
                        buildDatabase(context.applicationContext, DATABASE_NAME)
                    }).also {
                    instance = it
                }
            }

        // Used if building the database instead of copying an included .db file
        private fun seedAndBuildDatabase(context: Context, dbName: String): WordsetDatabase {
            return Room
                .databaseBuilder(context, WordsetDatabase::class.java, dbName)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        context.startService(Intent(context, WordsetDatabaseSeedService::class.java))
                    }
                })
                .build()
        }

        private fun buildDatabase(context: Context, dbName: String): WordsetDatabase {
            return Room
                .databaseBuilder(context, WordsetDatabase::class.java, "$dbName.db")
                .createFromAsset("databases/$dbName.db")
                .build()
        }
    }
}

