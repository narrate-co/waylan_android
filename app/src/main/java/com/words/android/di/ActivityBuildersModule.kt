package com.words.android.di

import com.words.android.MainActivity
import com.words.android.ui.common.BaseUserActivity
import com.words.android.ui.settings.SettingsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeBaseUserActivity(): BaseUserActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainFragmentBuildersModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SettingsFragmentBuildersModule::class])
    abstract fun contributeSettingsActivity(): SettingsActivity

}

