package space.narrate.words.android.di

import space.narrate.words.android.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthModule {
        @ContributesAndroidInjector
        abstract fun contributeAuthActivity(): AuthActivity
}

