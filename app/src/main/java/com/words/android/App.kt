package com.words.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseUser
import com.words.android.data.firestore.User
import com.words.android.di.AppInjector
import com.words.android.di.UserComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App: Application(), HasActivityInjector {


    companion object {
        const val REINJECT_USER_BROADCAST_ACTION = "reinject_user_broadcast_action"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var userComponentBuilder: UserComponent.Builder

    private var user: FirebaseUser? = null

    fun setUser(user: User?) {
        userComponentBuilder
                .user(user)
                .build()
                .inject(this)
        dispatchReinjectUserBroadcast()
    }

    fun clearUser() {
        user = null
        AppInjector.init(this)
        dispatchReinjectUserBroadcast()
    }


    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }


    private fun dispatchReinjectUserBroadcast() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(REINJECT_USER_BROADCAST_ACTION))
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector

}

