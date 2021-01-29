package space.narrate.waylan.android.util

import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import kotlin.math.abs
import kotlin.math.max

/**
 * A class that calculates the height of the on screen keyboard and exposes live data states
 * that clients can observe and react to.
 *
 * On Android 10 and below, this class adds an invisible popup window to the activity
 * and watches for the window to be displaced by an ime. On Android 11 and above, this class uses
 * the new WindowInsetsAnimation API to allow clients to smoothly animate views out of the way.
 */
class KeyboardManager(
    private val activity: FragmentActivity,
    private val targetView: View,
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

    // Keep track of any negative height when the keyboard is collapsed. This is added to the
    // expanded height of the keyboard to give the accurate, full height to any observers.
    // This is only needed on Android 10 and below but no-ops for Android 11 and above.
    private var additiveHeight = 0

    private val keyboardHeightData = MutableLiveData<SoftInputModel>()

    init {
        contentView = layout

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        inputMethodMode = INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        activity.lifecycle.addObserver(this)

        // Ping the keyboard height data to start any live data observers before the keyboard
        // height is changed.
        val orientation = getOrientation()
        notifyKeyboardHeightChanged(0, orientation)

        // On Android 11 and above, use the WindowInsetsAnimation API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val cb = object : WindowInsetsAnimation.Callback(DISPATCH_MODE_STOP) {
                override fun onProgress(
                    insets: WindowInsets,
                    runningAnimations: MutableList<WindowInsetsAnimation>
                ): WindowInsets {
                    handleWindowInsetsAnimationProgress(insets)
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimation) {
                    super.onEnd(animation)
                    // If the ime animation has been interrupted by an app exit or other event,
                    // make sure to update the keyboard height to the end value and avoid getting
                    // stuck midway through the animation.
                    val insets = targetView.rootWindowInsets
                    handleWindowInsetsAnimationProgress(insets)
                }
            }
            targetView.setWindowInsetsAnimationCallback(cb)
        } else {
            // On Android 10 and below, watch for layout changes and react to the popup window's
            // displacement.
            layout.viewTreeObserver.addOnGlobalLayoutListener {
                handleOnGlobalLayout()
            }
        }
    }

    fun getKeyboardHeightData(): LiveData<SoftInputModel> = keyboardHeightData

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        targetView.post {
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

    /**
     * Legacy: Calculate the displacement of the popup window caused by the ime.
     *
     * This is how the keyboard height is calculated on Android 10 and below.
     */
    private fun handleOnGlobalLayout() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        layout.getWindowVisibleDisplayFrame(rect)

        val orientation = getOrientation()
        val keyboardHeight = screenSize.y - rect.bottom

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        } else {
            notifyKeyboardHeightChanged(keyboardHeight, orientation)
        }
    }

    /**
     * Handle the change in ime insets using the ime type WindowInsets.
     *
     * This is how the keyboard height is calculated on Android 11 and above.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleWindowInsetsAnimationProgress(insets: WindowInsets) {
        val nonImeInsetBottom = insets
            .getInsets(WindowInsets.Type.navigationBars())
            .bottom
        val imeInsetBottom = insets.getInsets(WindowInsets.Type.ime()).bottom
        // Subtract any insets that are protecting the navigation area
        val keyboardHeight = max(0, imeInsetBottom - nonImeInsetBottom)
        val orientation = getOrientation()
        notifyKeyboardHeightChanged(keyboardHeight, orientation)
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardHeightData.value = SoftInputModel(
            calculateCorrectedHeight(height),
            orientation,
            isOpen(height)
        )
    }

    private fun getOrientation(): Int = activity.resources.configuration.orientation

    private fun isOpen(height: Int): Boolean = height > 0

    private fun calculateCorrectedHeight(height: Int): Int {
        return if (!isOpen(height)) {
            // Keyboard is considered collapsed
            additiveHeight = height;
            0
        } else {
            // Keyboard is considered expanded
            height + abs(additiveHeight)
        }
    }
}
