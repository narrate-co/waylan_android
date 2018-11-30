package com.wordsdict.android.ui.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wordsdict.android.App
import com.wordsdict.android.Navigator
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.Preferences
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseUserActivity: DaggerAppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private val localBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                App.RESET_ORIENTATION_BROADCAST -> setOrientation()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setOrientation()
        val app = application as App
        if (!app.hasUser) {
            app.setUser(null)
            Navigator.launchAuth(this)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, app.getLocalBroadcastIntentFilter())

        super.onCreate(savedInstanceState)
    }


    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
        super.onDestroy()
    }


    private fun setOrientation() {
        val orientation = PreferenceManager.getDefaultSharedPreferences(this).getInt(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED.value)
        requestedOrientation = orientation
    }
}

