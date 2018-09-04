package com.words.android.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.words.android.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.words.android.data.disk.*
import com.words.android.data.disk.wordset.*
import kotlinx.coroutines.experimental.launch
import org.threeten.bp.OffsetDateTime

/**
 * A Service to be run on first opening which loads the entire dictionary into our SQLite db...
 */
class DatabaseSeedService: Service() {

    companion object {
        const val DATA_SEED_NOTIFICATION_ID = 12345
        const val DATA_NOTIFICATION_CHANNEL_ID = "data_channel_id"
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        println("onStartCommand")

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


        //Begin work in a coroutine
        launch {
            seed()
        }

        return START_STICKY
    }

    private fun seed() {
        println("seed")
        //Get db instance
        val db = AppDatabase.getInstance(applicationContext)

        //Delete all data
        db.wordDao().deleteAll()

        //Seed all data from JSON
        val gson = Gson()
        val files = assets.list("")

        for (file in files) {
            // Get json string from asset file
            try {
                val jsonString = getJsonFromAsset(file)
                // Parse into BaseWord object map
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
                    //TODO
                    words.add(word)
//                    db.wordDao().insert(word)


                    println("inserted word. meanings size: ${value.meanings?.size}")
                    for (m in value.meanings ?: emptyList()) {
                        println("creating meaning")
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
                        println("inserting meaning")
                        //TODO
                        meanings.add(meaning)
                    }
                }

                db.wordDao().insertAll(*words.toTypedArray())
                db.meaningDao().insertAll(*meanings.toTypedArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        println("stopSeed")
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
        println("createDataNotificationChannel")
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


