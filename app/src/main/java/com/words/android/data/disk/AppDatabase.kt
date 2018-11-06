package com.words.android.data.disk

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.words.android.data.AppTypeConverters
import com.words.android.data.DatabaseSeedService
import com.words.android.data.disk.mw.Definition
import com.words.android.data.disk.mw.MwDao
import com.words.android.data.disk.wordset.Meaning
import com.words.android.data.disk.wordset.MeaningDao
import com.words.android.data.disk.wordset.Word
import com.words.android.data.disk.wordset.WordDao
import com.words.android.util.RoomAsset

@Database(entities = [
    (Word::class),
    (Meaning::class),
    (com.words.android.data.disk.mw.Word::class),
    (Definition::class)
], version = 2)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun meaningDao(): MeaningDao
    abstract fun mwDao(): MwDao

    companion object {

        @VisibleForTesting
        private const val DATABASE_NAME = "word-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                instance ?: synchronized(this) {
                    instance ?: buildDatabase(context.applicationContext, DATABASE_NAME).also {
                        instance = it
                    }
                }

        private fun seedAndBuildDatabase(context: Context, dbName: String): AppDatabase {
            return Room
                    .databaseBuilder(context, AppDatabase::class.java, "words-db")
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            context.startService(Intent(context, DatabaseSeedService::class.java))
                        }
                    })
                    .build()
        }

        private fun buildDatabase(context: Context, dbName: String): AppDatabase {
            return RoomAsset
                    .databaseBuilder(context, AppDatabase::class.java, "word-db.db")
                    .build()
        }

    }
}

