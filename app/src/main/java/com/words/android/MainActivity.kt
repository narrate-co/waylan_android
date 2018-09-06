package com.words.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.ui.details.DetailsFragment
import com.words.android.ui.home.HomeFragment
import com.words.android.ui.list.ListFragment
import com.words.android.ui.settings.SettingsActivity
import com.words.android.util.displayHeightDp
import com.words.android.util.displayHeightPx
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val detailsFragment by lazy { DetailsFragment.newInstance() }
    private val favoriteFragment by lazy { ListFragment.newInstance(ListFragment.ListType.FAVORITE) }
    private val recentFragment by lazy { ListFragment.newInstance(ListFragment.ListType.RECENT) }
    private val trendingFragment by lazy { ListFragment.newInstance(ListFragment.ListType.TRENDING) }

    private val bottomSheet by lazy { BottomSheetBehavior.from(searchFragment.view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .replace(R.id.fragmentContainer, homeFragment)
                    .commit()
        }



        searchFragment.view?.layoutParams?.height = Math.round(displayHeightPx * .60F)
        bottomSheet.setBottomSheetCallback(bottomSheetSkrimCallback)
    }

    override fun onBackPressed() {
        if (handleFragmentOnBackPressed()) return

        super.onBackPressed()
    }

    private fun handleFragmentOnBackPressed(): Boolean {
        if (bottomSheet.state != BottomSheetBehavior.STATE_HIDDEN && bottomSheet.state != BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            return true
        }

        return false
    }

    fun showDetails() {
        Navigator.showDetails(this, detailsFragment)
    }

    fun showListFragment(type: ListFragment.ListType) {
        Navigator.showListFragment(this, type, when (type) {
            ListFragment.ListType.TRENDING -> trendingFragment
            ListFragment.ListType.RECENT -> recentFragment
            ListFragment.ListType.FAVORITE -> favoriteFragment
        })
    }

    fun launchSettings() {
        //TODO move into Navigator
        startActivity(Intent(this, SettingsActivity::class.java))
    }


    private val bottomSheetSkrimCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, offset: Float) {
            bottomSheetSkrim.alpha = offset
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if ((newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) && bottomSheetSkrim.visibility != View.GONE) {
                bottomSheetSkrim.visibility = View.GONE
            } else {
                bottomSheetSkrim.visibility = View.VISIBLE
            }
        }
    }

}
