package com.wordsdict.android.data.disk

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wordsdict.android.data.disk.mw.Definition
import com.wordsdict.android.data.disk.mw.MwDao
import com.wordsdict.android.data.disk.wordset.Meaning
import com.wordsdict.android.data.disk.wordset.MeaningDao
import com.wordsdict.android.data.disk.wordset.Word
import com.wordsdict.android.data.disk.wordset.WordDao
import com.wordsdict.android.util.widget.RoomAsset

/**
 *
 * A [RoomDatabase] that provides the two local data sources - WordSet and previously
 * fetched (cached) Merriam-Webster entries
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
@Database(entities = [
    (Word::class),
    (Meaning::class),
    (com.wordsdict.android.data.disk.mw.Word::class),
    (Definition::class)
], version = 2)
@TypeConverters(RoomTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun meaningDao(): MeaningDao
    abstract fun mwDao(): MwDao

    companion object {

        @VisibleForTesting
        private const val DATABASE_NAME = "word-db"

        private const val SHOULD_SEED_DATABASE = false

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
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
        private fun seedAndBuildDatabase(context: Context, dbName: String): AppDatabase {
            return Room
                    .databaseBuilder(context, AppDatabase::class.java, dbName)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            context.startService(Intent(context, DatabaseSeedService::class.java))
                        }
                    })
                    .build()
        }

        // Copy the .db file on first load, otherwise return the AppDatabase instance
        private fun buildDatabase(context: Context, dbName: String): AppDatabase {
            return RoomAsset
                    .databaseBuilder(context, AppDatabase::class.java, "$dbName.db")
                    .build()
        }

    }
}

