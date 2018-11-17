package com.wordsdict.android.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.wordsdict.android.App
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector

object AppInjector {
    fun init(app: App) {
        DaggerAppComponent.builder()
                .application(app)
                .build()
                .inject(app)

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity?, p1: Bundle?) {
                handleActivity(activity)
            }
            override fun onActivityStarted(activity: Activity?) {}
            override fun onActivityResumed(activity: Activity?) {}
            override fun onActivityPaused(activity: Activity?) {}
            override fun onActivityStopped(activity: Activity?) {}
            override fun onActivityDestroyed(activity: Activity?) {}
            override fun onActivitySaveInstanceState(activity: Activity?, bundle: Bundle?) {}
        })
    }

    private fun handleActivity(activity: Activity?) {
        if (activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }
        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                    .registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                            injectFragment(f)
                        }

                        private fun injectFragment(f: Fragment) {
                            if (f is Injectable) {
                                AndroidSupportInjection.inject(f)
                            }
                        }

                    }, true)
        }
    }
}

