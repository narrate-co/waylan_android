package space.narrate.words.android

import android.app.Application
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import space.narrate.words.android.data.repository.AnalyticsRepository
import space.narrate.words.android.di.DaggerAppComponent
import space.narrate.words.android.di.UserComponent
import space.narrate.words.android.data.auth.Auth
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

/**
 * Base [Application] class which handles disptaching application wide event and the creation and
 * destruction of [UserComponent].
 */
class App: DaggerApplication() {

    companion object {
        const val RESET_USER_BROADCAST = "reset_user_broadcast"
    }

    @Inject
    lateinit var userComponentBuilder: UserComponent.Builder

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    /**
     * A helper variable to be used in user-dependent objects to ensure that a valid user is present
     * and user dependent objects are available to be injected.
     *
     * The system occasionally attempts to restart the Words process inside a [UserScope]d
     * components. This variable can confirm this has happened and signal the need to kick out
     * to the AuthActivity.
     */
    var hasUser: Boolean = false

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    /**
     * Set the current user an inject a new [UserComponent] to provide all user dependent objects.
     * [auth] is an optional [Auth] object. Passing null will "log out" and user dependent objects
     * may not function properly.
     */
    fun setUser(auth: Auth?) {
        analyticsRepository.setUserId(auth?.firebaseUser?.uid)
        userComponentBuilder
                .user(auth?.user)
                .firebaseUser(auth?.firebaseUser)
                .build()
                .inject(this)
        hasUser = auth != null
        dispatchReinjectUserBroadcast()
    }

    /**
     * Helper method to "log out" the user by passing null to [setUser]
     */
    fun clearUser() {
        setUser(null)
        dispatchReinjectUserBroadcast()
    }

    private fun dispatchReinjectUserBroadcast() {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(RESET_USER_BROADCAST))
    }


}

