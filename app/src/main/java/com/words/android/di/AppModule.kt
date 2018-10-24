package com.words.android.di

import android.app.Application
import com.words.android.data.disk.AppDatabase
import com.words.android.data.prefs.PreferenceRepository
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
}

