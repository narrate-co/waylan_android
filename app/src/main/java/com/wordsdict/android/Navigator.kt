package com.wordsdict.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import com.wordsdict.android.ui.auth.AuthActivity
import com.wordsdict.android.ui.details.DetailsFragment
import com.wordsdict.android.ui.home.HomeFragment
import com.wordsdict.android.ui.list.ListFragment
import com.wordsdict.android.ui.settings.*
import com.wordsdict.android.util.widget.ElasticTransition

/**
 * A static object to Navigate to or between destinations.
 *
 * This removes the need to remember parameters and extras at the call site, leading to
 * dryer more stabler code.
 *
 * Methods named with launch<destination> navigate to Activities or destinations outside of Words.
 * For example, navigating to [SettingsActivity] or opening a link in Chrome.
 *
 * Methods named with show<destination> perform fragment transactions on the current Activity's
 * [R.id.fragmentContainer].
 *
 * Note: When animating Fragments entering and exiting, it's beneficial to wait until after those
 * animations run before calling load intensive initialization work in the Fragment. There are two
 * types of Fragment animations used in Words, Fragment Transactions and Transitions. For
 * Transactions, [BaseUserFragment] handles adding an AnimationListener to the enter Transaction
 * and calling [BaseUserFragment.onEnterTransactionEnded]. For Transitions, a TransitionListener
 * is added to the transition set on a Fragment before it is commited. In the TransitionListener's
 * onTransitionEnd callback, [BaseUserFragment.onEnterTransitionEnded] is called.
 *
 * TODO move all fragments to use Transitions and remove the need for
 * TODO [BaseUserActivity.onEnterTransactionEnded] and supporting logic
 *
 */
object Navigator {

    /**
     * A list of all possible destinations which can be added to [MainActivity]'s
     * [R.id.fragmentContainer]
     */
    enum class HomeDestination {
        HOME, LIST, DETAILS
    }

    /**
     * Replace [MainActivity]'s fragment container with a [HomeFragment]. This method replaces the
     * fragment container because [HomeFragment] should be the root destination. Pressing back
     * from [HomeFragment] exists the app.
     *
     * @return whether the [HomeFragment] was added to [MainActivity]'s [R.id.fragmentContainer]
     */
    fun showHome(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                        R.id.fragmentContainer,
                        HomeFragment.newInstance(),
                        HomeFragment.FRAGMENT_TAG
                )
                .commit()
        return true
    }

    /**
     * Add [DetailsFragment] to [MainActivity]'s fragment container. This method does not
     * add a [DetailsFragment] if it is already the current fragment.
     *
     * @return true if there isn't already a [DetailsFragment] present and new [DetailsFragment]
     *  has been added to [MainActivity]'s fragment container
     */
    fun showDetails(activity: FragmentActivity): Boolean {
        // does a details fragment already exist as the current fragment?
        val existingDetailsFragment =
                activity.supportFragmentManager.findFragmentByTag(DetailsFragment.FRAGMENT_TAG)
        if (existingDetailsFragment == null || !existingDetailsFragment.isAdded) {

            val detailsFragment = DetailsFragment.newInstance()

            // Add an ElasticTransition for this fragment's enter, sliding up and alpha'ing in
            val enterTransition = ElasticTransition()
            enterTransition.interpolator = FastOutSlowInInterpolator()
            // TODO come up with a more robust transition listener solution that doesn't depend
            // TODO as heavily on the transition call site
            enterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionEnd(transition: Transition) {
                    // Ensure we call [BaseUserFragment.onEnterTransitionEnded] which
                    // [BaseUserFragment] subclasses depend on to know when animation has ended
                    // and load intensive  initialization work should start
                    detailsFragment.onEnterTransitionEnded()
                }
            })
            detailsFragment.enterTransition = enterTransition
            detailsFragment.reenterTransition = enterTransition

            // Add an ElasticTransition for this fragment's exist, sliding down and alpha'ing out
            val exitTransition = ElasticTransition()
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

    /**
     * Add a [ListFragment] of the specified [type] to [MainActivity]'s fragment container.
     *
     * Note: Unlike [showDetails], this method will add duplicate [ListFragment] if called when
     * a [ListFragment] is already the current [R.id.fragmentContainer] fragment
     *
     * @param type The [ListFragment.ListType] of the [ListFragment] to be added
     */
    fun showListFragment(activity: FragmentActivity, type: ListFragment.ListType): Boolean {

        // Determine what type of ListFragment we should be creating
        val listFragment = when (type) {
            ListFragment.ListType.TRENDING -> ListFragment.newTrendingInstance()
            ListFragment.ListType.RECENT -> ListFragment.newRecentInstance()
            ListFragment.ListType.FAVORITE -> ListFragment.newFavoriteInstance()
        }

        // Add an ElasticTransition for this fragment's enter, sliding up and alpha'ing in
        val enterTransition = ElasticTransition()
        enterTransition.interpolator = FastOutSlowInInterpolator()
        // TODO come up with a more robust transition listener solution that doesn't depend
        // TODO as heavily on the transition call site
        enterTransition.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                // Ensure we call [BaseUserFragment.onEnterTransitionEnded] which [BaseUserFragment]
                // subclasses depend on to know when animation has ended and load intensive
                // initialization work should start
                listFragment.onEnterTransitionEnded()
            }
        })
        listFragment.enterTransition = enterTransition
        listFragment.reenterTransition = enterTransition

        val exitTransition = ElasticTransition()
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


    /**
     * Replace [SettingsActivity]'s fragment container with [SettingsFragment].
     * [SettingsFragment] is the root fragment of [SettingsFragment]. Pressing back should lead back
     * to [MainActivity]
     *
     * @return true if the fragment container was replaced with [SettingsFragment]
     */
    fun showSettings(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        SettingsFragment.newInstance(),
                        SettingsFragment.FRAGMENT_TAG
                )
                .commit()
        return true
    }

    /**
     * Add [AboutFragment] to [SettingsActivity]'s fragment container.
     *
     * @return true if [AboutFragment] was added to [SettingsActivity]'s fragment container
     */
    fun showAbout(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_enter,
                        R.anim.fragment_exit,
                        R.anim.fragment_pop_enter,
                        R.anim.fragment_pop_exit
                )
                .add(
                        R.id.fragmentContainer,
                        AboutFragment.newInstance(),
                        AboutFragment.FRAGMENT_TAG
                )
                .addToBackStack(AboutFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    /**
     * Add [ThirdPartyLibrariesFragment] to [SettingsActivity]'s fragment container
     *
     * @return true if [ThirdPartyLibrariesFragment] was added to [SettingsActivity]'s fragment
     *  container
     */
    fun showThirdPartyLibraries(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_enter,
                        R.anim.fragment_exit,
                        R.anim.fragment_pop_enter,
                        R.anim.fragment_pop_exit
                )
                .add(
                        R.id.fragmentContainer,
                        ThirdPartyLibrariesFragment.newInstance(),
                        ThirdPartyLibrariesFragment.FRAGMENT_TAG
                )
                .addToBackStack(ThirdPartyLibrariesFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    /**
     * Add [DeveloperSettingsFragment] to [SettingsActivity]'s fragment container
     *
     * @return true if [DeveloperSettingsFragment] was added to [SettingsActivity]'s fragment
     *  container
     */
    fun showDeveloperSettings(activity: FragmentActivity): Boolean {
        activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fragment_enter,
                        R.anim.fragment_exit,
                        R.anim.fragment_pop_enter,
                        R.anim.fragment_pop_exit
                )
                .add(
                        R.id.fragmentContainer,
                        DeveloperSettingsFragment.newInstance(),
                        DeveloperSettingsFragment.FRAGMENT_TAG
                )
                .addToBackStack(DeveloperSettingsFragment.FRAGMENT_TAG)
                .commit()
        return true
    }

    /**
     * Launch [AuthActivity]. The passed [AuthActivity.AuthRoute] will be used by the launched
     * [AuthActivity] to determine what behavior should be expected/UI configuration should be
     * given.
     *
     * Note: [AuthActivity] and [RouterActivity] are the only two Activities which do not require
     * a [UserScope]/valid user.
     *
     * @param authRoute The type of action launching this [AuthActivity] should accomplish
     * @param filterIntent Any intent extras which should be passed through this [AuthActivity]
     *  and on to any subsequent destinations who wish to consume them. (ie. an
     *  Intent.ACTION_PROCESS_TEXT extra)
     */
    fun launchAuth(
            context: Context,
            authRoute: AuthActivity.AuthRoute? = null,
            filterIntent: Intent? = null
    ) {
        val intent = Intent(context, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (authRoute != null) intent.putExtra(AuthActivity.AUTH_ROUTE_EXTRA_KEY, authRoute.name)
        if (filterIntent != null) {
            intent.putExtras(filterIntent)
        }
        context.startActivity(intent)
    }

    /**
     * Launch [MainActivity].
     *
     * @param clearStack true if pressing back from this new [MainActivity] should exit the app
     * @param filterIntent Any intent extras which should be passed to this [MainActivity]. These
     *  may be intents which were received by [RouterActivity] or [AuthActivity] and should now
     *  be handled by [MainActivity], such as Intent.ACTION_PROCCESS_TEXT extras.
     */
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

    /**
     * Launch [SettingsActivity]
     */
    fun launchSettings(context: Context) {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }

    /**
     * Launch the user's default email client directly into a newly composed email.
     *
     * @param toEmail The addressee's email address
     * @param subject The email title
     * @param body The email's content
     * @param shareTitle The title of the share picker sheet the client will be offered to choose
     *  their desired email client
     * @throws ActivityNotFoundException If the user doesn't have an email client installed, this
     *  method will throw an ActivityNotFound exception
     */
    @Throws(ActivityNotFoundException::class)
    fun launchEmail(context: Context, toEmail: String, subject: String = "", body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO)
        val mailTo = "mailto:$toEmail?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
        intent.data = Uri.parse(mailTo)
        context.startActivity(intent)
    }

    /**
     * Open a [url] with the default browser
     */
    fun launchWebsite(context: Context, url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(browserIntent)
    }
}

