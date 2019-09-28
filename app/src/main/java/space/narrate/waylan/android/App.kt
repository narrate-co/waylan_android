package space.narrate.waylan.android

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import space.narrate.waylan.android.data.prefs.PreferenceStore
import space.narrate.waylan.android.data.prefs.Preferences
import space.narrate.waylan.android.di.appModule

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}

