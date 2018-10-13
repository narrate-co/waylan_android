package com.words.android.di

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.words.android.data.disk.AppDatabase
import com.words.android.data.firestore.FirestoreStore
import com.words.android.data.firestore.User
import com.words.android.data.mw.MerriamWebsterStore
import com.words.android.data.mw.RetrofitService
import com.words.android.data.repository.WordRepository
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityBuildersModule::class, ViewModelModule::class])
class UserModule {

    @UserScope
    @Provides
    fun provideWordRepository(application: Application, user: User?): WordRepository {
        val appDatabase = AppDatabase.getInstance(application)
        val firestoreStore: FirestoreStore? = if (user?.firebaseUser != null) FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, user.firebaseUser) else null
        val merriamWebsterStore: MerriamWebsterStore? = if (user?.isMerriamWebsterSubscriber == true) MerriamWebsterStore(RetrofitService.getInstance(), appDatabase.mwDao()) else null
        return WordRepository(appDatabase, firestoreStore, merriamWebsterStore)
    }

}

