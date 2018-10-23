package com.words.android.di

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.words.android.data.disk.AppDatabase
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User
import com.words.android.data.mw.MerriamWebsterStore
import com.words.android.data.mw.RetrofitService
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.repository.WordRepository
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityBuildersModule::class, ViewModelModule::class])
class UserModule {

    @UserScope
    @Provides
    fun provideWordRepository(application: Application, user: User?, firebaseUser: FirebaseUser?): WordRepository {
        val appDatabase = AppDatabase.getInstance(application)
        val firestoreStore: FirestoreStore? = if (firebaseUser != null) FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, firebaseUser) else null
        val merriamWebsterStore = when (user?.merriamWebsterState) {
            PluginState.FREE_TRIAL,
            PluginState.PURCHASED -> MerriamWebsterStore(RetrofitService.getInstance(), appDatabase.mwDao())
            else -> null
        }
        return WordRepository(appDatabase, firestoreStore, merriamWebsterStore, user)
    }

}

