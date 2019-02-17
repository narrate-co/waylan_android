package space.narrate.words.android.ui.common

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.di.Injectable
import space.narrate.words.android.util.invisible
import space.narrate.words.android.util.visible
import dagger.android.support.DaggerFragment
import space.narrate.words.android.ui.common.BaseUserActivity
import javax.inject.Inject

/**
 * A Fragment that falls under [UserScope]. User dependent objects are available to this
 * Fragment.
 */
abstract class BaseUserFragment: DaggerFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    /**
     * If using fragment Transaction animations, set an AnimationListener on the enter transition
     * and provide an overridable method to be called once the transaction has ended.
     *
     * Delaying Fragment initialization until after all transactions have ended helps avoid
     * doing too much work on the main thread and causing jank while running animations
     */
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        var animation = super.onCreateAnimation(transit, enter, nextAnim)
        if (animation == null && nextAnim != 0) {
            animation = AnimationUtils.loadAnimation(activity, nextAnim)
        }
        if (animation != null) {
            view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            animation.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }
                override fun onAnimationEnd(p0: Animation?) {
                    view?.setLayerType(View.LAYER_TYPE_NONE, null)
                    onEnterTransactionEnded()
                }
                override fun onAnimationStart(p0: Animation?) {
                }
            })
        } else {
            onEnterTransactionEnded()
        }

        return animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            // if this view is being restored by the system, no animations will be present and
            // transaction/transition end callbacks should be manually called immediately
            onEnterTransactionEnded()
            onEnterTransitionEnded()
        } else {
            // We're assuming the fragment has either transition or a transaction to run
            // give views a chance to be laid out and drawn before trying to run a transition
            postponeEnterTransition()
            val parent = view.parent as? ViewGroup
            if (parent != null) {
                parent.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            } else {
                onEnterTransactionEnded()
                onEnterTransitionEnded()
            }
        }
    }

    /**
     * Called after the current Fragment's enter [FragmentTransaction] has ended. If none is set,
     * this will be called in [Fragment.onViewCreated]
     */
    open fun onEnterTransactionEnded() { }

    /**
     * Called after the current Fragment's [Fragment.getEnterTransition] has ended. If none is set,
     * this will be called in [Fragment.onViewCreated].
     *
     * This is currently set and called when adding a transition to a fragment transaction
     * @see [Navigatior.showDetails].
     */
    open fun onEnterTransitionEnded() { }

    /**
     * A helper method common to many Words Fragments that initializes a view which acts as a
     * status bar background. The view is only visible when [appBarLayout] is <i>not</i> fully
     * extended. This makes it possible to have a transparent status bar when dragging to dismiss
     * a Fragment using [ElasticAppBarBehaivor] and a non-transparent status bar when content is
     * scrolling under the status bar and should not be visible.
     */
    fun setUpStatusBarScrim(scrim: View, appBarLayout: AppBarLayout? = null) {
        scrim.setOnApplyWindowInsetsListener { v, insets ->
            v.layoutParams.height = insets.systemWindowInsetTop
            insets
        }

        appBarLayout?.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    if (verticalOffset == 0) scrim.invisible() else scrim.visible()
                }
        )
    }

    /**
     * Set's [BaseUserActivity.unconsumedNavigationMethod] to [method] which will be optionally
     * consumed when [BaseUserActivity.onBackPressed] is called if configured to log
     * [AnalyticsRepository.EVENT_NAVIGATE_BACK] events.
     */
    fun setUnconsumedNavigationMethod(method: NavigationMethod) {
        (activity as? BaseUserActivity)?.unconsumedNavigationMethod = method
    }

}

