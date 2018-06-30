package com.words.android

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.words.android.data.disk.AppDatabase
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.repository.WordRepository

class App: Application() {

    private val appDatabase: AppDatabase by lazy { AppDatabase.getInstance(this) }

    lateinit var wordRepository: WordRepository

    val viewModelFactory: ViewModelFactory by lazy { ViewModelFactory(this) }

    /**
     * user is set from AuthActivity.
     * if user is null, we're operating in 'guest' mode and all firebase enabled functionality
     * should be disallowed. This would only occur if the user doesn't have internet to
     * sign in anonymously and should rarely be the case.
     */
    var user: FirebaseUser? = null
        set(value) {
            field = value
            if (value != null) {
                firestoreStore = FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, value)
            } else {
                firestoreStore = null
            }

            wordRepository = WordRepository(appDatabase, firestoreStore)
        }

    var firestoreStore: FirestoreStore? = null

    override fun onCreate() {
        super.onCreate()
        appDatabase.init()
        wordRepository = WordRepository(appDatabase, firestoreStore)
    }


}

