package com.wordsdict.android.di

import com.wordsdict.android.MainActivity
import com.wordsdict.android.ui.common.BaseUserActivity
import com.wordsdict.android.ui.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainFragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SettingsFragmentBuildersModule::class])
    abstract fun contributeSettingsActivity(): SettingsActivity

}

