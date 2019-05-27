package space.narrate.words.android.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import space.narrate.words.android.BuildConfig
import space.narrate.words.android.ui.MainViewModel
import space.narrate.words.android.billing.BillingManager
import space.narrate.words.android.data.auth.AuthenticationStore
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.firestore.FirestoreStore
import space.narrate.words.android.data.mw.MerriamWebsterStore
import space.narrate.words.android.data.mw.RetrofitService
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.prefs.RotationManager
import space.narrate.words.android.data.prefs.ThirdPartyLibraryStore
import space.narrate.words.android.data.prefs.UserPreferenceStore
import space.narrate.words.android.data.repository.AnalyticsRepository
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.data.spell.SymSpellStore
import space.narrate.words.android.ui.auth.AuthViewModel
import space.narrate.words.android.ui.details.DetailsViewModel
import space.narrate.words.android.ui.dev.DeveloperSettingsViewModel
import space.narrate.words.android.ui.home.HomeViewModel
import space.narrate.words.android.ui.list.ListViewModel
import space.narrate.words.android.ui.search.SearchViewModel
import space.narrate.words.android.ui.settings.SettingsViewModel

val appModule = module {

    // Stores
    single { AppDatabase.getInstance(androidContext()) }

    single { PreferenceStore(androidContext()) }

    single { SymSpellStore(androidContext()) }

    single { ThirdPartyLibraryStore }

    single { AuthenticationStore(FirebaseAuth.getInstance(), get()) }

    single { FirestoreStore(FirebaseFirestore.getInstance(), get()) }

    single { MerriamWebsterStore(RetrofitService.getInstance(), get<AppDatabase>().mwDao()) }

    single { UserPreferenceStore(androidContext(), get()) }

    // Repositories
    single {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(androidContext()).apply {
            setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
        AnalyticsRepository(firebaseAnalytics, get())
    }

    single { WordRepository(get(), get(), get(), get(), get()) }

    single { UserRepository(get(), get(), get(), get(), get()) }

    // Managers
    single { RotationManager(androidContext()) }

    single { BillingManager(androidContext(), get(), get()) }

    viewModel { MainViewModel(get(), get(), get()) }

    viewModel { HomeViewModel(get()) }

    viewModel { SearchViewModel(get(), get(), get()) }

    viewModel { SettingsViewModel(get()) }

    viewModel { DeveloperSettingsViewModel(get()) }

    viewModel { ListViewModel(get(), get()) }

    viewModel { DetailsViewModel(get(), get()) }

    viewModel { AuthViewModel(get(), get(), get()) }
}