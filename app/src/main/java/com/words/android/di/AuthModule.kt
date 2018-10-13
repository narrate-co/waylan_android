package com.words.android.di

import com.words.android.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthModule {

        @ContributesAndroidInjector
        abstract fun contributeAuthActivity(): AuthActivity

}

