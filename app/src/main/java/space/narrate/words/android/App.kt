package space.narrate.words.android

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import space.narrate.words.android.data.prefs.PreferenceStore
import space.narrate.words.android.data.prefs.Preferences
import space.narrate.words.android.di.appModule

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

    }
}

