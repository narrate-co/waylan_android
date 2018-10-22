package com.words.android.ui.common

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.words.android.App
import com.words.android.R
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class BaseActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val usesDarkMode = (application as? App)?.preferenceRepository?.usesDarkMode ?: false
        println("BaseActivity::usesDarkMode - $usesDarkMode")
        setTheme(if (usesDarkMode) R.style.Theme_Words_Dark else R.style.Theme_Words_Light)

        super.onCreate(savedInstanceState)
    }

}

