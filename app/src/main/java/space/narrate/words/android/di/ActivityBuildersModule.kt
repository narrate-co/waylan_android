package space.narrate.words.android.di

import space.narrate.words.android.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        MainFragmentBuildersModule::class,
        SettingsFragmentBuildersModule::class
    ])
    abstract fun contributeMainActivity(): MainActivity


}

