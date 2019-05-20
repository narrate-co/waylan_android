package space.narrate.words.android.di

import android.app.Application
import space.narrate.words.android.App
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

/**
 * __________________________________________________________
 * |                                                        |
 * |  @ApplicationScope                                     |
 * |   (App, AuthActivity, RouterActivity)                  |
 * |    _________________________________________________   |
 * |    |                                               |   |
 * |    |   @UserScope                                  |   |
 * |    |    (Owns all objects which require            |   |
 * |    |     a user)                                   |   |
 * |    |   _________________________________________   |   |
 * |    |   |                                       |   |   |
 * |    |   |   @ActivityScope                      |   |   |
 * |    |   |    (MainActivity, SettingsActivity)   |   |   |
 * |    |   |   _____________________________       |   |   |
 * |    |   |   |                           |       |   |   |
 * |    |   |   |   @FragmentScope          |       |   |   |
 * |    |   |   |    (All fragments owned   |       |   |   |
 * |    |   |   |     by @ActivityScope     |       |   |   |
 * |    |   |   |     Activities)           |       |   |   |
 * |    |   |   |___________________________|       |   |   |
 * |    |   |                                       |   |   |
 * |    |   |_______________________________________|   |   |
 * |    |                                               |   |
 * |    |_______________________________________________|   |
 * |                                                        |
 * |________________________________________________________|
 *
 * TODO Refine this since it doesn't make perfect sense (ie. AuthActivity should have @ActivityScope
 * but not @UserScope like MainActivity. @ActivityScope might need to be a scope that overlaps
 * @UserScope and @ApplicationScope
 */
@Component(
        modules = [
            AppModule::class,
            AuthModule::class,
            AndroidSupportInjectionModule::class
        ]
)
@ApplicationScope
interface AppComponent: AndroidInjector<App> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}

