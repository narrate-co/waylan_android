package com.words.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.ui.common.BaseUserActivity
import com.words.android.ui.list.ListFragment
import com.words.android.ui.search.SearchSheetCallback
import com.words.android.util.*
import kotlinx.android.synthetic.main.main_activity.*

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
        setContentView(R.layout.main_activity)


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
        viewModel.popBackStack()
        super.onBackPressed()
    }

    private fun setUpSearchSheet() {
        searchFragment.view?.layoutParams?.height = Math.round(displayHeightPx * .60F)
        searchSheetCallback.addOnSlideAction { _, fl -> bottomSheetSkrim.alpha = fl }
        searchSheetCallback.addOnStateChangedAction { _, newState ->
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> {
                    bottomSheetSkrim.gone()
                }
                else -> bottomSheetSkrim.visible()
            }
        }
        bottomSheet.setBottomSheetCallback(searchSheetCallback)
        bottomSheetSkrim.setOnClickListener {
            bottomSheet.collapse(this)
        }
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
