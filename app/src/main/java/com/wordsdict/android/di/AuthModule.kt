package com.wordsdict.android.di

import com.wordsdict.android.ui.auth.AuthActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthModule {
        @ContributesAndroidInjector
        abstract fun contributeAuthActivity(): AuthActivity
}

