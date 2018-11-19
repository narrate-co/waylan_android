package com.wordsdict.android.ui.common

import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class BaseUserActivity: DaggerAppCompatActivity(), HasSupportFragmentInjector {
//
//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

//    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector

}

