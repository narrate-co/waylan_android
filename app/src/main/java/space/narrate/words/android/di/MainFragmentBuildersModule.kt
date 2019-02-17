package space.narrate.words.android.di

import space.narrate.words.android.ui.details.DetailsFragment
import space.narrate.words.android.ui.home.HomeFragment
import space.narrate.words.android.ui.list.ListFragment
import space.narrate.words.android.ui.search.ContextualFragment
import space.narrate.words.android.ui.search.SearchFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentBuildersModule {

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeHomeFragment(): HomeFragment

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeSearchFragment(): SearchFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeContextualFragment(): ContextualFragment

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeDetailsFragment(): DetailsFragment

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeListFragment(): ListFragment


}

