package space.narrate.words.android.di

import space.narrate.words.android.ui.about.AboutFragment
import space.narrate.words.android.ui.dev.DeveloperSettingsFragment
import space.narrate.words.android.ui.settings.SettingsFragment
import space.narrate.words.android.ui.third_party.ThirdPartyLibrariesFragment
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

    @FragmentScope
    @ContributesAndroidInjector //add fragment specific dependencies here
    abstract fun contributeThirdPartyLibrariesFragment(): ThirdPartyLibrariesFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeveloperSettingsFragment(): DeveloperSettingsFragment

}

