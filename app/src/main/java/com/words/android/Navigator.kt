package com.words.android

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.SettingsActivity

object Navigator {

    fun showDetails(activity: FragmentActivity, detailsFragment: DetailsFragment) {
        //replace
        val existingDetailsFragment = activity.supportFragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG)
        if (existingDetailsFragment == null || !existingDetailsFragment.isAdded) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                    .replace(R.id.fragmentContainer, detailsFragment, DetailsFragment.FRAGMENT_TAG)
                    .addToBackStack(DetailsFragment.FRAGMENT_TAG)
                    .commit()
        }
    }

    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType, listFragment: ListFragment) {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                .replace(R.id.fragmentContainer, listFragment, type.fragmentTag)
                .addToBackStack(type.fragmentTag)
                .commit()
    }

    fun launchSettings(activity: FragmentActivity) {
        activity.startActivity(Intent(activity, SettingsActivity::class.java))
    }


}

