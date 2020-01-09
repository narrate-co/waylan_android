package space.narrate.waylan.merriamwebster_thesaurus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import space.narrate.waylan.core.util.RoomTypeConverters

/**
 * Room database which acts as an offline cache for Merriam-Webster Thesaurus data.
 */
@Database(
    entities = [ThesaurusEntry::class],
    version = 1
)
@TypeConverters(RoomTypeConverters::class)
abstract class ThesaurusDatabase : RoomDatabase() {

    abstract fun thesaurusDao(): ThesaurusDao

    companion object {
        private const val DATABASE_NAME = "merriam-webster-thesaurus-db"

        @Volatile
        private var instance: ThesaurusDatabase? = null

        fun getInstance(context: Context): ThesaurusDatabase =
            instance ?: synchronized(this) {
                instance ?: (buildDatabase(context.applicationContext, DATABASE_NAME)).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context, dbName: String): ThesaurusDatabase =
            Room
                .databaseBuilder(context, ThesaurusDatabase::class.java, dbName)
                .fallbackToDestructiveMigration()
                .build()
    }
}