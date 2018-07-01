package com.words.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.words.android.ui.dashboard.DashboardFragment
import com.words.android.ui.details.DetailsFragment

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
