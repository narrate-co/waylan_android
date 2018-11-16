package com.words.android.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.words.android.data.analytics.AnalyticsRepository
import com.words.android.data.disk.AppDatabase
import com.words.android.data.prefs.PreferenceRepository
import com.words.android.data.spell.SymSpell
import com.words.android.data.spell.SymSpellStore
import dagger.Module
import dagger.Provides

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
}

