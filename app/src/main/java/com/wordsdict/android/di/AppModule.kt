package com.wordsdict.android.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.wordsdict.android.App
import com.wordsdict.android.billing.BillingManager
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.disk.AppDatabase
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.spell.SymSpellStore
import com.wordsdict.android.util.OrientationManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(subcomponents = [UserComponent::class])
class AppModule {

    @ApplicationScope
    @Provides
    fun provideAppDatabase(application: Application): AppDatabase {
        return AppDatabase.getInstance(application)
    }

    @ApplicationScope
    @Provides
    fun providePreferenceRepository(application: Application): PreferenceRepository {
        return PreferenceRepository(application)
    }

    @ApplicationScope
    @Provides
    fun provideSymSpellStore(application: Application): SymSpellStore {
        return SymSpellStore(application.applicationContext)
    }

    @ApplicationScope
    @Provides
    fun provideAnalyticsRepository(application: Application): AnalyticsRepository {
        return AnalyticsRepository(FirebaseAnalytics.getInstance(application))
    }

    @ApplicationScope
    @Provides
    fun provideOrientationManager(application: Application): OrientationManager {
        return OrientationManager(application)
    }

}

