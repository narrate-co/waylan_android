package space.narrate.waylan.android

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import space.narrate.waylan.android.di.appModule
import space.narrate.waylan.core.di.coreModule
import space.narrate.waylan.settings.di.settingsModule
import space.narrate.waylan.wordnik.data.di.wordnikModule

class App: Application() {

    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(listOf(coreModule, appModule, settingsModule, wordnikModule))
        }
    }
}

