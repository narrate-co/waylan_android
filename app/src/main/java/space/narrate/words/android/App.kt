package space.narrate.words.android

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import space.narrate.words.android.data.analytics.AnalyticsRepository
import space.narrate.words.android.data.prefs.Preferences
import space.narrate.words.android.di.DaggerAppComponent
import space.narrate.words.android.di.UserComponent
import space.narrate.words.android.ui.auth.Auth
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

        // A broadcast to indicate that [AppCompatDelegate.getDefaultNightMode] has changed and
        // Activities should use [AppCompatDelegate.setLocalNightMode] to trigger a configuration
        //change
        const val RESET_NIGHT_MODE_BROADCAST = "reset_night_mode_broadcast"

        // A broadcast to indicate that [Preferences.ORIENTATION_LOCK] has changed and Activities
        // should request the new orientation found in the preference
        const val RESET_ORIENTATION_BROADCAST = "reset_orientation_broadcast"
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

    override fun onCreate() {
        updateNightMode()
        super.onCreate()
    }

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

    /**
     * Set the app's default night mode to the value found in [Preferences.NIGHT_MODE]. This
     * will trigger a LocalBroadcast which can be received by any Activity that wishes to set
     * its use [AppCompatDelegate.setLocalNightMode] to force a configuration change.
     */
    fun updateNightMode() {
        val nightMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(Preferences.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            dispatchResetNightModeBroadcast()
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    /**
     * Trigger a LocalBroadcast indicating the value of [Preferences.ORIENTATION_LOCK] has changed.
     * This broadcast can be received by any Activity who wishes to handle manually calling
     * [AppCompatActivity.requestOrientation] and setting the value to the value found in
     * [Preferences.ORIENTATION_LOCK].
     */
    fun updateOrientation() {
        dispatchResetOrientationBroadcast()
    }

    /**
     * A centralized function to get an IntentFilter which includes all action filters for
     * local Words broadcasts.
     *
     * @return an [IntentFilter] that includes all actions locally dispatched by [App]
     */
    fun getLocalBroadcastIntentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(RESET_USER_BROADCAST)
        filter.addAction(RESET_ORIENTATION_BROADCAST)
        filter.addAction(RESET_NIGHT_MODE_BROADCAST)
        return filter
    }

    private fun dispatchReinjectUserBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(RESET_USER_BROADCAST))
    }

    private fun dispatchResetOrientationBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(RESET_ORIENTATION_BROADCAST))
    }

    private fun dispatchResetNightModeBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(RESET_NIGHT_MODE_BROADCAST))
    }


}

