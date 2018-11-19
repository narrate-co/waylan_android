package com.wordsdict.android.di

import com.wordsdict.android.ui.common.BaseUserFragment
import com.wordsdict.android.ui.details.DetailsFragment
import com.wordsdict.android.ui.home.HomeFragment
import com.wordsdict.android.ui.list.ListFragment
import com.wordsdict.android.ui.search.SearchFragment
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
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeDetailsFragment(): DetailsFragment

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeListFragment(): ListFragment


}

