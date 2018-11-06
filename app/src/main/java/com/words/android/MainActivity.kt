package com.words.android

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.ui.common.BaseUserActivity
import com.words.android.ui.list.ListFragment
import com.words.android.util.collapse
import com.words.android.util.displayHeightPx
import com.words.android.util.gone
import com.words.android.util.visible
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseUserActivity() {

    private val bottomSheet by lazy { BottomSheetBehavior.from(searchFragment.view) }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        println("MainActivity::textToProcess = $textToProcess")
        if (!textToProcess.isNullOrBlank()) {
            viewModel.setCurrentWordId(textToProcess.toString())
            showDetails()
        }
    }

    override fun onBackPressed() {
        if (handleFragmentOnBackPressed()) return
        super.onBackPressed()
    }

    private fun setUpSearchSheet() {
        searchFragment.view?.layoutParams?.height = Math.round(displayHeightPx * .60F)
        bottomSheet.setBottomSheetCallback(bottomSheetSkrimCallback)
        bottomSheetSkrim.setOnClickListener {
            bottomSheet.collapse(this)
        }
    }

    private fun handleFragmentOnBackPressed(): Boolean {
        return bottomSheet.collapse(this)
    }

    private fun showHome() = Navigator.showHome(this)

    fun showDetails() = Navigator.showDetails(this)

    fun showListFragment(type: ListFragment.ListType) = Navigator.showListFragment(this, type)

    fun launchSettings() = Navigator.launchSettings(this)


    private val bottomSheetSkrimCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, offset: Float) {
            bottomSheetSkrim.alpha = offset
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HIDDEN -> {
                    bottomSheetSkrim.gone()
                }
                else -> bottomSheetSkrim.visible()
            }
        }
    }

}
