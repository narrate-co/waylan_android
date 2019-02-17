package space.narrate.words.android.di

import android.app.Application
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import space.narrate.words.android.billing.BillingManager
import space.narrate.words.android.data.analytics.AnalyticsRepository
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.mw.MerriamWebsterStore
import space.narrate.words.android.data.mw.RetrofitService
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.prefs.UserPreferenceStore
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.data.spell.SymSpellStore
import dagger.Module
import dagger.Provides

@Module(
        includes = [ActivityBuildersModule::class, ViewModelModule::class]
)
class UserModule {

    @UserScope
    @Provides
    fun provideFirestoreStore(appDatabase: AppDatabase, firebaseUser: FirebaseUser?): FirestoreStore? {
        return if (firebaseUser != null) FirestoreStore(FirebaseFirestore.getInstance(), appDatabase, firebaseUser) else null
    }

    @UserScope
    @Provides
    fun provideMerriamWebsterStore(appDatabase: AppDatabase, analyticsRepository: AnalyticsRepository): MerriamWebsterStore {
        return MerriamWebsterStore(RetrofitService.getInstance(), appDatabase.mwDao(), analyticsRepository)
    }

    @UserScope
    @Provides
    fun provideWordRepository(
            appDatabase: AppDatabase,
            firestoreStore: FirestoreStore?,
            merriamWebsterStore: MerriamWebsterStore,
            symSpellStore: SymSpellStore
    ): WordRepository {

        return WordRepository(appDatabase, firestoreStore, merriamWebsterStore, symSpellStore)
    }

    @UserScope
    @Provides
    fun provideUserPreferenceRepository(application: Application, user: User?): UserPreferenceStore {
        return UserPreferenceStore(application, user?.uid)
    }

    @UserScope
    @Provides
    fun provideUserRepository(
            firestoreStore: FirestoreStore?,
            userPreferenceStore: UserPreferenceStore,
            preferenceStore: PreferenceStore): UserRepository {
        return UserRepository(firestoreStore, userPreferenceStore, preferenceStore)
    }

    @UserScope
    @Provides
    fun provideBillingManager(application: Application, userRepository: UserRepository): BillingManager {
        return BillingManager(application, userRepository)
    }

}

