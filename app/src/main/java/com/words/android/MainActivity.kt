package com.words.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.words.android.ui.dashboard.DashboardFragment
import com.words.android.ui.details.DetailsFragment
import com.words.android.util.hideSoftKeyboard
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, (application as App).viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val dashboardFragment by lazy { DashboardFragment.newInstance() }
    private val detailsFragment by lazy { DetailsFragment.newInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, dashboardFragment)
                    .commit()
        }

        sharedViewModel.currentWord.observe(this, Observer {
            hideSoftKeyboard()
        })


        BottomSheetBehavior.from(searchFragment.view).setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {
                println("BottomSheetCallback::onStateChanged - $p1")
            }
            override fun onSlide(p0: View, p1: Float) {
                println("BottomSheetCallback::onSlide - $p1")
            }
        })
    }

    fun showDetails() {
        if (!detailsFragment.isAdded) {
            //replace
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, detailsFragment)
                    .addToBackStack("details_fragment_stack_tag")
                    .commit()
        }
    }

}
