package com.wordsdict.android.ui.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wordsdict.android.App
import com.wordsdict.android.Navigator
import com.wordsdict.android.data.analytics.NavigationMethod
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.Preferences
import com.wordsdict.android.util.doOnNextResume
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * An AppCompatActivity that falls under [UserScope]. User dependent objects are available to this
 * Activity.
 */
abstract class BaseUserActivity: DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // listen for local broadcasts that should trigger changes in resumed activities
    private val localBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                App.RESET_ORIENTATION_BROADCAST -> setOrientation()
                App.RESET_NIGHT_MODE_BROADCAST -> setLocalNightMode()
            }
        }
    }

    /**
     * A holding property for the last method used to navigate back. This can be consumed by the
     * subclassing Activity if configured to report [AnalyticsRepository.EVENT_NAVIGATE_BACK]
     *
     * This variable will be set when any view or child (view/fragment) of this Activity opts to
     * report that a back navigation event has occurred. For example, if a Fragment has it's own
     * back navigation icon, if the user taps on that icon and the fragment first calls
     * [BaseUserFragment.setUnconsumedNavigationMethod] and then [AppCompatActivity.onBackPressed],
     * this property should be set to [NavigationMethod.NAV_ICON]. Alternatively if
     * [AppCompatActivity.onBackPressed] is called and this property is null, it can only be
     * assumed that either the call site forgot to set the unconsumed navigation method or (and
     * more likely) the back event originated from the navigation bar back button, which is only
     * reported to the current Activity.
     *
     * TODO delegate this to a "Consumable" that nulls out the value after a get()
     */
    var unconsumedNavigationMethod: NavigationMethod? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setOrientation()
        val app = application as App

        // TODO rework Dagger configuration to avoid this?
        // When the app's process is killed and restarted, the system occasionally attempts
        // to restore the app directly into a UserScope'ed state. If this happens, injecting
        // will fail. This check avoids injection crashing by setting a null user and setting
        // a temporary, invalid user and kicking out to AuthActivity
        if (!app.hasUser) {
            app.setUser(null)
            Navigator.launchAuth(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(localBroadcastReceiver, app.getLocalBroadcastIntentFilter())

        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
        super.onDestroy()
    }

    private fun setOrientation() {
        val orientation = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.value)

        requestedOrientation = orientation
    }

    private fun setLocalNightMode() {

        val nightMode = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getInt(Preferences.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Make sure we are at least in the resumed state so we can properly handle the config
        // change which this will trigger. Some methods like [HomeFragment.setUpReachabilityParams
        // will not set proper values if this is run while paused.
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            delegate.setLocalNightMode(nightMode)
        } else {
            lifecycle.doOnNextResume {
                delegate.setLocalNightMode(nightMode)
            }
        }
    }


}

