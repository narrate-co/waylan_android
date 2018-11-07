package com.words.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.words.android.ui.about.AboutFragment
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.DeveloperSettingsFragment
import com.words.android.ui.settings.SettingsActivity
import com.words.android.ui.settings.SettingsFragment
import java.util.*

object Navigator {

    enum class HomeDestination {
        HOME, LIST, DETAILS
    }

    fun showHome(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment.newInstance(), HomeFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    fun showDetails(activity: FragmentActivity): Boolean {
        //replace
        val existingDetailsFragment = activity.supportFragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG)
        if (existingDetailsFragment == null || !existingDetailsFragment.isAdded) {
            activity.supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                    .add(R.id.fragmentContainer, DetailsFragment.newInstance(), DetailsFragment.FRAGMENT_TAG)
                    .addToBackStack(DetailsFragment.FRAGMENT_TAG)
                    .commit()
            return true
        } else {
            return false
        }
    }

    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType): Boolean {
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
        return true
    }

    fun showSettings(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, SettingsFragment.newInstance(), SettingsFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    fun showAbout(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                .add(R.id.fragmentContainer, AboutFragment.newInstance(), AboutFragment.FRAGMENT_TAG)
                .addToBackStack(AboutFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    fun showDeveloperSettings(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit)
                .add(R.id.fragmentContainer, DeveloperSettingsFragment.newInstance(), DeveloperSettingsFragment.FRAGMENT_TAG)
                .addToBackStack(DeveloperSettingsFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    fun launchSettings(context: Context) {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    /**
     * @param toEmail The addressee's email address
     * @param subject The email title
     * @param body The email's content
     * @param shareTitle The title of the share picker sheet the client will be offered to choose their desired email client
     * @throws ActivityNotFoundException If the user doesn't have an email client installed, this method will throw an ActivityNotFound exception
     */
    @Throws(ActivityNotFoundException::class)
    fun launchEmail(context: Context, toEmail: String, subject: String = "", body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO)
        val mailTo = "mailto:$toEmail?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
        intent.data = Uri.parse(mailTo)
        context.startActivity(intent)
    }

}

