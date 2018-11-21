package com.wordsdict.android.di

import com.wordsdict.android.ui.about.AboutFragment
import com.wordsdict.android.ui.settings.DeveloperSettingsFragment
import com.wordsdict.android.ui.settings.SettingsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class SettingsFragmentBuildersModule {

//    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeSettingsFragment(): SettingsFragment

//    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeAboutFragment(): AboutFragment

//    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeveloperSettingsFragment(): DeveloperSettingsFragment

}

