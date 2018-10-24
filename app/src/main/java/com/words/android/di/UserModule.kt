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
import com.words.android.data.prefs.UserPreferenceRepository
import com.words.android.data.repository.UserRepository
import com.words.android.data.repository.WordRepository
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityBuildersModule::class, ViewModelModule::class])
class UserModule {

    @UserScope
    @Provides
    fun provideFirestoreStore(appDatabase: AppDatabase, firebaseUser: FirebaseUser?): FirestoreStore? {
        return if (firebaseUser != null) FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, firebaseUser) else null
    }

    @UserScope
    @Provides
    fun provideMerriamWebsterStore(appDatabase: AppDatabase): MerriamWebsterStore {
        return MerriamWebsterStore(RetrofitService.getInstance(), appDatabase.mwDao())
    }

    @UserScope
    @Provides
    fun provideWordRepository(appDatabase: AppDatabase, firestoreStore: FirestoreStore?, merriamWebsterStore: MerriamWebsterStore): WordRepository {

        return WordRepository(appDatabase, firestoreStore, merriamWebsterStore)
    }

    @UserScope
    @Provides
    fun provideUserPreferenceRepository(application: Application, user: User?): UserPreferenceRepository {
        return UserPreferenceRepository(application, user?.uid)
    }

    @UserScope
    @Provides
    fun provideUserRepository(
            firestoreStore: FirestoreStore?,
            userPreferenceRepository: UserPreferenceRepository): UserRepository {
        return UserRepository(firestoreStore, userPreferenceRepository)
    }

}

