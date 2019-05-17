package space.narrate.words.android.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.di.Injectable
import space.narrate.words.android.util.invisible
import space.narrate.words.android.util.visible
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A Fragment that falls under [UserScope]. User dependent objects are available to this
 * Fragment.
 */
abstract class BaseUserFragment: DaggerFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            handleApplyWindowInsets(insets)
        }
        ViewCompat.requestApplyInsets(view)
    }

    open fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        return insets
    }

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

}

