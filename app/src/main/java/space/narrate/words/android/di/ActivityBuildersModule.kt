package space.narrate.words.android.di

import space.narrate.words.android.MainActivity
import space.narrate.words.android.ui.settings.SettingsActivity
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

