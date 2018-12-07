package com.wordsdict.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wordsdict.android.data.analytics.NavigationMethod
import com.wordsdict.android.ui.common.BaseUserActivity
import com.wordsdict.android.ui.list.ListFragment
import com.wordsdict.android.ui.search.SearchFragment
import com.wordsdict.android.ui.search.SearchSheetCallback
import com.wordsdict.android.util.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseUserActivity() {

    val searchSheetCallback = SearchSheetCallback()

    private val bottomSheet by lazy {
        BottomSheetBehavior.from(searchFragment.view)
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            showHome()
        }

        processText(intent)

        setUpSearchSheet()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        processText(intent)
    }

    private fun processText(intent: Intent?) {
        val textToProcess = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        if (!textToProcess.isNullOrBlank()) {
            viewModel.setCurrentWordId(textToProcess.toString())
            showDetails()
        }
    }

    override fun onBackPressed() {
        if (handleFragmentOnBackPressed()) return
        viewModel.popBackStack(unconsumedNavigationMethod)
        unconsumedNavigationMethod = null
        super.onBackPressed()
    }

    private fun setUpSearchSheet() {

        //Set max expanded height to 60% of screen height (max reachability area)
        searchFragment.view?.layoutParams?.height = Math.round(displayHeightPx * .60F)

        // Set scrim alpha on slide
        searchSheetCallback.addOnSlideAction { _, fl -> bottomSheetSkrim.alpha = fl }

        // Set scrim visibility on sheet collapsed/shown
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> {
                    bottomSheetSkrim.gone()
                }
                else -> bottomSheetSkrim.visible()
            }
        }

        // Hide keyboard if sheet is manually collapsed
        searchSheetCallback.addOnStateChangedAction { view, newState ->
            if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                //make sure keyboard is down
                hideSoftKeyboard()
            }
        }

        // collapse sheet if scrim is touched
        bottomSheetSkrim.setOnClickListener {
            bottomSheet.collapse(this)
        }

        bottomSheet.setBottomSheetCallback(searchSheetCallback)
    }

    /**
     * Any child Fragment of this Activity can call this method to have the SearchFragment
     * bottom sheet expanded, the SearchFragment's search input field focused and the IME opened to
     * initiate a new search.
     *
     * This is used by [ListFragment] when a user clicks on a banner that prompts them to
     * "Get Started" searching for words.
     */
    fun focusAndOpenSearch() {
        val searchFragment = supportFragmentManager.findFragmentById(R.id.searchFragment) as? SearchFragment
        searchFragment?.focusAndOpenSearch()
    }

    private fun handleFragmentOnBackPressed(): Boolean {
        return bottomSheet.collapse(this)
    }

    private fun showHome() {
        if (Navigator.showHome(this)) {
            viewModel.pushToBackStack(Navigator.HomeDestination.HOME)
        }
    }

    fun showDetails() {
        if (Navigator.showDetails(this)) {
            viewModel.pushToBackStack(Navigator.HomeDestination.DETAILS)
        }
    }

    fun showListFragment(type: ListFragment.ListType) {
        if (Navigator.showListFragment(this, type)) {
            viewModel.pushToBackStack(Navigator.HomeDestination.LIST)
        }
    }

    fun launchSettings() = Navigator.launchSettings(this)

}
