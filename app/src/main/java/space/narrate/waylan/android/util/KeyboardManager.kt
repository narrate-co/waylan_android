package space.narrate.waylan.android.util

import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent

/**
 * A class that attempts to calculate the height of the on screen keyboard in order to
 * set dependent UI values to keep content visible.
 *
 * TODO This isn't a very robust solution. Find something better
 */
class KeyboardManager(
        private val activity: FragmentActivity,
        private val activityRoot: View
) : PopupWindow(activity), LifecycleObserver {

    private val root: View = activity.findViewById(android.R.id.content)

    private val layout: FrameLayout by lazy {
        val fl = FrameLayout(activity)
        fl.layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        )
        fl.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent))
        fl
    }

    private var keyboardHeightPortrait: Int = 0

    private var keyboardHeightLandscape: Int = 0

    data class KeyboardHeightData(val height: Int, val orientation: Int)

    private val keyboardHeightData = MutableLiveData<KeyboardHeightData>()

    init {

        contentView = layout

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED


        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        activity.lifecycle.addObserver(this)

        layout.viewTreeObserver.addOnGlobalLayoutListener {
            handleOnGlobalLayout()
        }
    }

    fun getKeyboardHeightData(): LiveData<KeyboardHeightData> = keyboardHeightData

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        activityRoot.post {
            if (!isShowing && root.windowToken != null) {
                setBackgroundDrawable(ColorDrawable(0))
                showAtLocation(root, Gravity.NO_GRAVITY, 0, 0)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        dismiss()
    }

    private fun handleOnGlobalLayout() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        layout.getWindowVisibleDisplayFrame(rect)

        //TODO use onApplyWindowInsets and factor that height in
        val orientation = activity.resources.configuration.orientation
        val keyboardHeight = screenSize.y - rect.bottom

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            keyboardHeightPortrait = keyboardHeight
            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        } else {
            keyboardHeightLandscape = keyboardHeight
            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        }
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardHeightData.value = KeyboardHeightData(
            height,
            orientation
        )
    }
}