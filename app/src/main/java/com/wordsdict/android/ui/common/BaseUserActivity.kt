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
import com.wordsdict.android.util.RotationManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseUserActivity: DaggerAppCompatActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

//    @Inject
//    lateinit var rotationManager: RotationManager

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

//        rotationManager.observe(BaseUserActivity::class.java.name, this, object: RotationManager.Observer {
//            override fun onLockedRotate(last: Int, new: Int) {
//                println("BaseUserActivity::onLockedRotate - last: $last, new: $new")
//            }
//
//            override fun onUnlockedOrientationChange(last: Int, new: Int) {
//                println("BaseUserActivity::onUnlockedOrientationChange - last: $last, new: $new")
//            }
//        })


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

