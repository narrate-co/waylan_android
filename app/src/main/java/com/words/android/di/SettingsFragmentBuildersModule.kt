package com.words.android.di

import com.words.android.ui.about.AboutFragment
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.search.SearchFragment
import com.words.android.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentBuildersModule {

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeSettingsFragment(): SettingsFragment

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeAboutFragment(): AboutFragment

}

