package space.narrate.waylan.core.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import space.narrate.waylan.core.BuildConfig
import space.narrate.waylan.core.data.firestore.AuthenticationStore
import space.narrate.waylan.core.data.firestore.FirestoreStore
import space.narrate.waylan.core.data.prefs.PreferenceStore
import space.narrate.waylan.core.data.prefs.RotationManager
import space.narrate.waylan.core.data.prefs.UserPreferenceStore
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.data.spell.SymSpellStore
import space.narrate.waylan.core.data.wordset.WordsetDatabase

val coreModule = module {

    // Stores
    single { WordsetDatabase.getInstance(androidContext()) }

    single { PreferenceStore(androidContext()) }

    single { SymSpellStore(androidContext()) }

    single { AuthenticationStore(FirebaseAuth.getInstance(), get()) }

    single { FirestoreStore(FirebaseFirestore.getInstance(), get()) }

    single { UserPreferenceStore(androidContext(), get()) }

    // Repositories
    single {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(androidContext()).apply {
            setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
        AnalyticsRepository(firebaseAnalytics, get())
    }

    single { WordRepository(get(), get(), get(), get(), Dispatchers.IO) }

    single { UserRepository(get(), get(), get(), get()) }

    // Managers
    single { RotationManager(androidContext()) }
}