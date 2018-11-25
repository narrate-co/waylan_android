package com.wordsdict.android.ui.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wordsdict.android.App
import com.wordsdict.android.Navigator
import com.wordsdict.android.data.firestore.users
import com.wordsdict.android.data.prefs.Orientation
import com.wordsdict.android.data.prefs.PreferenceRepository
import com.wordsdict.android.data.prefs.Preferences
import dagger.android.support.DaggerAppCompatActivity
import dagger.android.support.HasSupportFragmentInjector
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
        val orientation = Orientation.valueOf(
                PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(Preferences.ORIENTATION_LOCK, Orientation.UNSPECIFIED?.name) ?: Orientation.UNSPECIFIED.name)
        requestedOrientation = orientation.value
    }



}

