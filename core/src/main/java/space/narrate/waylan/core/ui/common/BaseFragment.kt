package space.narrate.waylan.core.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

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

