package com.words.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.transition.Transition
import com.words.android.ui.about.AboutFragment
import com.words.android.ui.auth.Auth
import com.words.android.ui.auth.AuthActivity
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.DeveloperSettingsFragment
import com.words.android.ui.settings.SettingsActivity
import com.words.android.ui.settings.SettingsFragment
import com.words.android.util.ElasticTransition
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

object Navigator {

    enum class HomeDestination {
        HOME, LIST, DETAILS
    }

    fun showHome(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, HomeFragment.newInstance(), HomeFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    fun showDetails(activity: FragmentActivity): Boolean {
        //replace
        val existingDetailsFragment = activity.supportFragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG)
        if (existingDetailsFragment == null || !existingDetailsFragment.isAdded) {


            val detailsFragment = DetailsFragment.newInstance()

            val enterTransition = ElasticTransition(true)
            enterTransition.interpolator = FastOutSlowInInterpolator()
            enterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    detailsFragment.onEnterTransitionEnded()
                }
            })
            detailsFragment.enterTransition = enterTransition
            detailsFragment.reenterTransition = enterTransition

            val exitTransition = ElasticTransition(false)
            exitTransition.interpolator = FastOutSlowInInterpolator()
            detailsFragment.exitTransition = exitTransition
            detailsFragment.returnTransition = exitTransition

            activity.supportFragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainer, detailsFragment, DetailsFragment.FRAGMENT_TAG)
                    .addToBackStack(DetailsFragment.FRAGMENT_TAG)
                    .commit()
            return true
        } else {
            return false
        }
    }

    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType): Boolean {

        val listFragment = when (type) {
            ListFragment.ListType.TRENDING -> ListFragment.newTrendingInstance()
            ListFragment.ListType.RECENT -> ListFragment.newRecentInstance()
            ListFragment.ListType.FAVORITE -> ListFragment.newFavoriteInstance()
        }

        val enterTransition = ElasticTransition(true)
        enterTransition.interpolator = FastOutSlowInInterpolator()
        enterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                listFragment.onEnterTransitionEnded()
            }
        })
        listFragment.enterTransition = enterTransition
        listFragment.reenterTransition = enterTransition

        val exitTransition = ElasticTransition(false)
        exitTransition.interpolator = FastOutSlowInInterpolator()
        listFragment.exitTransition = exitTransition
        listFragment.returnTransition = exitTransition

        activity.supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentContainer, listFragment, type.fragmentTag)
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

    fun launchAuth(context: Context, authRoute: AuthActivity.AuthRoute? = null, filterIntent: Intent? = null) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (authRoute != null) intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }

    fun launchMain(context: Context, clearStack: Boolean, filterIntent: Intent?) {
        val intent = Intent(context, MainActivity::class.java)
        if (clearStack) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }
}

