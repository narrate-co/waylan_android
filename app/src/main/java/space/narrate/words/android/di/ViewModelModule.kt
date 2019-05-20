package space.narrate.words.android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.ui.auth.AuthViewModel
import space.narrate.words.android.ui.details.DetailsViewModel
import space.narrate.words.android.ui.home.HomeViewModel
import space.narrate.words.android.ui.list.ListViewModel
import space.narrate.words.android.ui.search.SearchViewModel
import space.narrate.words.android.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import space.narrate.words.android.ui.dev.DeveloperSettingsViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    internal abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    internal abstract fun bindHomeViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    internal abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel
//
//    @Binds
//    @IntoMap
//    @ViewModelKey(AuthViewModel::class)
//    internal abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    internal abstract fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DeveloperSettingsViewModel::class)
    internal abstract fun bindDeveloperSettingsViewModel(
        developerSettingsViewModel: DeveloperSettingsViewModel
    ): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ListViewModel::class)
    internal abstract fun bindListViewModel(listViewModel: ListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DetailsViewModel::class)
    internal abstract fun bindDetailsViewModel(detailsViewModel: DetailsViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(
        factory: WordsViewModelFactory
    ): ViewModelProvider.Factory

}

