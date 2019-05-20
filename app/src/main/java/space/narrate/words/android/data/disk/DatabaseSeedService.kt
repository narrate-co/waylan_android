package space.narrate.words.android.data.disk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import space.narrate.words.android.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime
import space.narrate.words.android.data.disk.wordset.*
import kotlin.coroutines.CoroutineContext

/**
 * A helper [Service] that loads, converts and inserts WordSet json dictionary files into
 * [WordDao]. This is only used if a pre-built Room db file is not included (and to be copied) or
 * if we need to build the database from scratch, extract it and include an updated version.
 *
 * After the database has been seeded, use the following command to extract the .db file and copy
 * it to assets/databases/:
 *
 * adb -d shell "run-as space.narrate.words.android cat databases/word-db" > word-db.db
 *
 * To toggle whether this Service is run, use [AppDatabase.SHOULD_SEED_DATABASE].
 */
class DatabaseSeedService: Service(), CoroutineScope {

    companion object {
        const val DATA_SEED_NOTIFICATION_ID = 12345
        const val DATA_NOTIFICATION_CHANNEL_ID = "data_channel_id"

        const val TAG = "DatabaseSeedService"
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        //Set up notification
        createDataNotificationChannel()

        val notification = NotificationCompat.Builder(this, DATA_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.data_seed_notification_title))
                .setContentText(getString(R.string.data_seed_notification_text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_logo_notif)
                .setProgress(0, 0, true)
                .build()

        startForeground(DATA_SEED_NOTIFICATION_ID, notification)



        launch {
            seed()
        }

        return START_STICKY
    }


    private fun seed() {
        //Get db instance
        val db = AppDatabase.getInstance(applicationContext)

        //Delete all data
        db.wordDao().deleteAll()

        //Seed all data from JSON
        val gson = Gson()
        val files = assets.list("wordset")

        for (file in files) {
            // Get json string from asset file
            try {
                val jsonString = getJsonFromAsset("wordset/$file")
                // Parse into BaseWord object mapTransform
                val letter: Map<String, BaseWord> = gson.fromJson(jsonString, object: TypeToken<Map<String, BaseWord>>() {}.type)

                val words: MutableList<Word> = mutableListOf()
                val meanings: MutableList<Meaning> = mutableListOf()

                for ((_, value) in letter) {
                    val word = Word(
                            value.word,
                            0,
                            OffsetDateTime.now(),
                            OffsetDateTime.now()
                    )
                    //insert word
                    words.add(word)

                    for (m in value.meanings ?: emptyList()) {
                        val meaning = Meaning(
                                value.word,
                                m.def ?: "",
                                if (m.example == null) emptyList() else listOf(Example(m.example, OffsetDateTime.now(), OffsetDateTime.now())),
                                m.speechPart ?: "unknown part of speech",
                                m.synonyms?.filter { it != null }?.map { Synonym(it!!, OffsetDateTime.now(), OffsetDateTime.now()) }
                                        ?: emptyList(),
                                value.labels?.map {
                                    Label(it.name
                                            ?: "unknown region", it.isDialect
                                            ?: false, OffsetDateTime.now(), OffsetDateTime.now())
                                } ?: emptyList()
                        )
                        //insert meaning
                        meanings.add(meaning)
                    }
                }

                db.wordDao().insertAll(*words.toTypedArray())
                db.meaningDao().insertAll(*meanings.toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }





        stopForeground(true)
        stopSelf()
    }

    private fun getJsonFromAsset(name: String): String {
        return resources.assets.open(name).use {
            val buffer = ByteArray(it.available())
            it.read(buffer)
            String(buffer, Charsets.UTF_8)
        }
    }

    private fun createDataNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Create notification channel
            val name = getString(R.string.data_notification_channel_name)
            val desc = getString(R.string.data_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(DATA_NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = desc

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    data class BaseWord(
            @SerializedName("word") val word: String,
            @SerializedName("wordset_id") val wordsetId: String?,
            @SerializedName("meanings") val meanings: List<BaseMeaning>?,
            @SerializedName("editors") val editors: List<String?>?,
            @SerializedName("contributors") val contributors: List<String?>?,
            @SerializedName("labels") val labels: List<BaseLabel>?
    )

    data class BaseMeaning(
            @SerializedName("id") val id: String?,
            @SerializedName("def") val def: String?,
            @SerializedName("example") val example: String?,
            @SerializedName("speech_part") val speechPart: String?,
            @SerializedName("synonyms") val synonyms: List<String?>?
    )

    data class BaseLabel(
            @SerializedName("name") val name: String?,
            @SerializedName("is_dialect") val isDialect: Boolean?
    )


}


