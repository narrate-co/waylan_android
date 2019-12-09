package space.narrate.waylan.android.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import space.narrate.waylan.android.AppNavigator
import space.narrate.waylan.android.BuildConfig
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.ui.auth.AuthViewModel
import space.narrate.waylan.android.ui.details.DetailsViewModel
import space.narrate.waylan.android.ui.details.ExampleDetailDataProvider
import space.narrate.waylan.android.ui.details.ExamplesDetailItemProvider
import space.narrate.waylan.android.ui.details.TitleDetailDataProvider
import space.narrate.waylan.android.ui.details.TitleDetailItemProvider
import space.narrate.waylan.android.ui.details.WordsetDetailDataProvider
import space.narrate.waylan.android.ui.details.WordsetDetailItemProvider
import space.narrate.waylan.settings.DeveloperSettingsViewModel
import space.narrate.waylan.android.ui.home.HomeViewModel
import space.narrate.waylan.android.ui.list.ListViewModel
import space.narrate.waylan.android.ui.search.SearchViewModel
import space.narrate.waylan.core.billing.BillingManager
import space.narrate.waylan.core.data.auth.AuthenticationStore
import space.narrate.waylan.core.data.firestore.FirestoreStore
import space.narrate.waylan.core.data.prefs.PreferenceStore
import space.narrate.waylan.core.data.prefs.RotationManager
import space.narrate.waylan.core.data.prefs.UserPreferenceStore
import space.narrate.waylan.core.data.repo.AnalyticsRepository
import space.narrate.waylan.core.data.repo.UserRepository
import space.narrate.waylan.core.data.repo.WordRepository
import space.narrate.waylan.core.data.spell.SymSpellStore
import space.narrate.waylan.core.data.wordset.WordsetDatabase
import space.narrate.waylan.core.details.DetailDataProviderRegistry
import space.narrate.waylan.core.details.DetailItemProviderRegistry
import space.narrate.waylan.core.details.DetailProviderFactory
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.settings.SettingsViewModel

val appModule = module {

    // Navigator
    single() { AppNavigator(get()) as Navigator }

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

    single { WordRepository(get(), get(), get(), get()) }

    single { UserRepository(get(), get(), get(), get()) }

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

    // Use reflection to get an instance of :merriamwebster's DetailProviderFactory.
    // TODO Move this into :merriamwebster when mw becomes not installed at runtime.
    single(named("merriamWebsterDetailProviderFactory")) {
        Class.forName(
            "space.narrate.waylan.merriamwebster.di.MerriamWebsterModuleProviderFactory"
        ).newInstance() as DetailProviderFactory
    }

    single(named("merriamWebsterThesaurusDetailDataProviderFactory")) {
        Class.forName(
            "space.narrate.waylan.merriamwebster_thesaurus.di.MerriamWebsterThesaurusModuleProviderFactory"
        ).newInstance() as DetailProviderFactory
    }

    // DetailDataProviderRegistry
    single {
        val detailDataProviderRegistry = DetailDataProviderRegistry()

        val merriamWebsterDetailProviderFactory: DetailProviderFactory =
            get(named("merriamWebsterDetailProviderFactory"))

        val merriamWebsterThesaurusDetailProviderFactory: DetailProviderFactory =
            get(named("merriamWebsterThesaurusDetailDataProviderFactory"))

        detailDataProviderRegistry.addProviders(
            TitleDetailDataProvider(get()),
            merriamWebsterDetailProviderFactory.getDetailDataProvider(),
            merriamWebsterThesaurusDetailProviderFactory.getDetailDataProvider(),
            WordsetDetailDataProvider(get()),
            ExampleDetailDataProvider(get())
        )

        detailDataProviderRegistry
    }

    // DetailItemProviderRegistry
    single {
        val detailItemFactory = DetailItemProviderRegistry()

        val merriamWebsterDetailProviderFactory: DetailProviderFactory =
            get(named("merriamWebsterDetailProviderFactory"))

        val merriamWebsterThesaurusDetailProviderFactory: DetailProviderFactory =
            get(named("merriamWebsterThesaurusDetailDataProviderFactory"))

        // Add the Merriam-Webster provider to the detailItemFactory
        detailItemFactory.addProviders(
            TitleDetailItemProvider(),
            merriamWebsterDetailProviderFactory.getDetailItemProvider(),
            merriamWebsterThesaurusDetailProviderFactory.getDetailItemProvider(),
            WordsetDetailItemProvider(),
            ExamplesDetailItemProvider()
        )

        detailItemFactory
    }

}