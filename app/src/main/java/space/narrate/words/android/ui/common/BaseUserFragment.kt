package space.narrate.words.android.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import space.narrate.words.android.di.Injectable
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
}

