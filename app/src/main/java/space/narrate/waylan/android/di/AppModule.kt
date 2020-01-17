package space.narrate.waylan.android.di

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import space.narrate.waylan.android.AppNavigator
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.ui.auth.AuthViewModel
import space.narrate.waylan.android.ui.details.DetailsViewModel
import space.narrate.waylan.android.ui.details.ExampleDetailDataProvider
import space.narrate.waylan.android.ui.details.ExamplesDetailItemProvider
import space.narrate.waylan.android.ui.details.WordsetDetailDataProvider
import space.narrate.waylan.android.ui.details.WordsetDetailItemProvider
import space.narrate.waylan.android.ui.home.HomeViewModel
import space.narrate.waylan.android.ui.list.ListViewModel
import space.narrate.waylan.android.ui.search.SearchViewModel
import space.narrate.waylan.core.details.DetailDataProviderRegistry
import space.narrate.waylan.core.details.DetailItemProviderRegistry
import space.narrate.waylan.core.details.DetailProviderFactory
import space.narrate.waylan.core.ui.Navigator

// Dependent on CoreModule
val appModule = module {

    // Navigator
    single { AppNavigator(get()) as Navigator }

    // ViewModels
    viewModel { MainViewModel(get(), get(), get()) }

    viewModel { HomeViewModel(get()) }

    viewModel { SearchViewModel(get(), get(), get(), get()) }

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
            merriamWebsterDetailProviderFactory.getDetailItemProvider(),
            merriamWebsterThesaurusDetailProviderFactory.getDetailItemProvider(),
            WordsetDetailItemProvider(),
            ExamplesDetailItemProvider()
        )

        detailItemFactory
    }
}