package com.words.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.ui.common.BaseUserActivity
import com.words.android.ui.list.ListFragment
import com.words.android.ui.search.SearchSheetCallback
import com.words.android.util.*
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
        if (!(application as App).hasUser) {
            Navigator.launchAuth(this, null, intent)
            finish()
        }

        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (savedInstanceState == null) {
            showHome()
        }

        println("MainActivity::onCreate - savedInstanceState = $savedInstanceState")
        processText(intent)

        setUpSearchSheet()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("MainActivity::onNewIntent")
        processText(intent)
    }

    private fun processText(intent: Intent?) {
        val textToProcess = intent?.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        println("MainActivity::processText - $textToProcess")
        if (!textToProcess.isNullOrBlank()) {
            viewModel.setCurrentWordId(textToProcess.toString())
            showDetails()
        }
    }

    override fun onBackPressed() {
        if (handleFragmentOnBackPressed()) return
        viewModel.popBackStack()
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
