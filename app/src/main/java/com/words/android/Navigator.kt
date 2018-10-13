package com.words.android

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.SettingsActivity

object Navigator {

    fun showHome(activity: FragmentActivity) {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                .replace(R.id.fragmentContainer, HomeFragment.newInstance(), HomeFragment.FRAGMENT_TAG)
                .commit()
    }

    fun showDetails(activity: FragmentActivity) {
        //replace
        val existingDetailsFragment = activity.supportFragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG)
        if (existingDetailsFragment == null || !existingDetailsFragment.isAdded) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                    .add(R.id.fragmentContainer, DetailsFragment.newInstance(), DetailsFragment.FRAGMENT_TAG)
                    .addToBackStack(DetailsFragment.FRAGMENT_TAG)
                    .commit()
        }
    }

    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType) {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                .add(R.id.fragmentContainer, when (type) {
                    ListFragment.ListType.TRENDING -> ListFragment.newTrendingInstance()
                    ListFragment.ListType.RECENT -> ListFragment.newRecentInstance()
                    ListFragment.ListType.FAVORITE -> ListFragment.newFavoriteInstance()
                }, type.fragmentTag)
                .addToBackStack(type.fragmentTag)
                .commit()
    }

    fun launchSettings(activity: FragmentActivity) {
        activity.startActivity(Intent(activity, SettingsActivity::class.java))
    }


}

