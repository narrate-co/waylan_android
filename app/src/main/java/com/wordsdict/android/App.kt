package com.wordsdict.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.prefs.Preferences
import com.wordsdict.android.di.DaggerAppComponent
import com.wordsdict.android.di.UserComponent
import com.wordsdict.android.ui.auth.Auth
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App: Application(), HasActivityInjector {

    companion object {
        const val REINJECT_USER_BROADCAST = "reinject_user_broadcast"
        const val RESET_ORIENTATION_BROADCAST = "reset_orientation_broadcast"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var userComponentBuilder: UserComponent.Builder

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector

    var hasUser: Boolean = false

    override fun onCreate() {
        updateDefaultNightMode()
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)
    }

    fun setUser(auth: Auth?) {
        analyticsRepository.setUserId(auth?.firebaseUser?.uid)
        userComponentBuilder
                .user(auth?.user)
                .firebaseUser(auth?.firebaseUser)
                .build()
                .inject(this)
        hasUser = true
        dispatchReinjectUserBroadcast()
    }

    fun clearUser() {
        setUser(null)
        DaggerAppComponent.builder().application(this).build().inject(this)
        dispatchReinjectUserBroadcast()
        hasUser = false
    }

    fun updateDefaultNightMode() {
        val nightMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(Preferences.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    fun updateOrientation() {
        dispatchResetOrientationBroadcast()
    }

    fun getLocalBroadcastIntentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(REINJECT_USER_BROADCAST)
        filter.addAction(RESET_ORIENTATION_BROADCAST)
        return filter
    }

    private fun dispatchReinjectUserBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(REINJECT_USER_BROADCAST))
    }

    private fun dispatchResetOrientationBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(RESET_ORIENTATION_BROADCAST))
    }


}

