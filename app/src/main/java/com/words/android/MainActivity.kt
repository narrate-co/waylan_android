package com.words.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment

class MainActivity : AppCompatActivity() {

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, (application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val detailsFragment by lazy { DetailsFragment.newInstance() }
    private val favoriteFragment by lazy { ListFragment.newInstance(ListFragment.ListType.FAVORITE) }
    private val recentFragment by lazy { ListFragment.newInstance(ListFragment.ListType.RECENT) }
    private val trendingFragment by lazy { ListFragment.newInstance(ListFragment.ListType.TRENDING) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .replace(R.id.fragmentContainer, homeFragment)
                    .commit()
        }
    }

    fun showDetails() {
        if (!detailsFragment.isAdded) {
            //replace
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack("details_fragment_stack_tag")
                    .commit()
        }
    }

    fun showListFragment(type: ListFragment.ListType) {
        supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(R.id.fragmentContainer, when (type) {
                    ListFragment.ListType.TRENDING -> trendingFragment
                    ListFragment.ListType.RECENT -> recentFragment
                    ListFragment.ListType.FAVORITE -> favoriteFragment
                })
                .addToBackStack(type.fragmentTag)
                .commit()
    }

}
