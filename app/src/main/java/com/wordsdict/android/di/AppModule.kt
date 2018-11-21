package com.wordsdict.android.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.wordsdict.android.App
import com.wordsdict.android.billing.BillingManager
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.disk.AppDatabase
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.spell.SymSpellStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [UserComponent::class])
class AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(application: Application): AppDatabase {
        return AppDatabase.getInstance(application)
    }

    @Singleton
    @Provides
    fun providePreferenceRepository(application: Application): PreferenceRepository {
        return PreferenceRepository(application)
    }

    @Singleton
    @Provides
    fun provideSymSpellStore(application: Application): SymSpellStore {
        return SymSpellStore(application.applicationContext)
    }

    @Singleton
    @Provides
    fun provideAnalyticsRepository(application: Application): AnalyticsRepository {
        return AnalyticsRepository(FirebaseAnalytics.getInstance(application))
    }

}

