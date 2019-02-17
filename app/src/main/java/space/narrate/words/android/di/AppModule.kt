package space.narrate.words.android.di

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import space.narrate.words.android.data.analytics.AnalyticsRepository
import space.narrate.words.android.di.UserComponent
import space.narrate.words.android.data.disk.AppDatabase
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.spell.SymSpellStore
import space.narrate.words.android.data.prefs.RotationManager
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
    fun providePreferenceRepository(application: Application): PreferenceStore {
        return PreferenceStore(application)
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
    fun provideRotationManager(application: Application): RotationManager {
        return RotationManager(application)
    }

}

