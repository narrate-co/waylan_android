package com.words.android.data.disk

import android.content.Context
import android.content.Intent
import android.util.Log
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

@Database(entities = [
    (Word::class),
    (Meaning::class),
    (com.words.android.data.disk.mw.Word::class),
    (Definition::class)
], version = 2)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase: RoomDatabase() {

    fun init() {
        Log.d("AppDatabase", "Initializing...")
    }

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

        private fun buildDatabase(context: Context, dbName: String): AppDatabase {
            return Room
                    .databaseBuilder(context, AppDatabase::class.java, dbName)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("Creating App Database...")
                            context.startService(Intent(context, DatabaseSeedService::class.java))
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
        }

    }
}

