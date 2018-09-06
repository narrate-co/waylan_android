package com.words.android

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment

object Navigator {

    fun showDetails(activity: FragmentActivity, detailsFragment: DetailsFragment) {
        if (!detailsFragment.isAdded) {
            //replace
            activity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack(DetailsFragment.FRAGMENT_TAG)
                    .commit()
        }

    }

    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType, listFragment: ListFragment) {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                .replace(R.id.fragmentContainer, listFragment)
                .addToBackStack(type.fragmentTag)
                .commit()
    }



}

