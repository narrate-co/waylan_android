package com.words.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.words.android.R
import com.words.android.data.firestore.users.PluginState
import com.words.android.data.firestore.users.User
import com.words.android.data.firestore.users.merriamWebsterState
import com.words.android.data.firestore.users.oneDayPastExpiration
import com.words.android.ui.common.BaseUserFragment
import com.words.android.util.configError
import com.words.android.util.configInformative
import kotlinx.android.synthetic.main.developer_settings_fragment.view.*
import kotlinx.android.synthetic.main.settings_item_layout.view.*
import java.util.*


class DeveloperSettingsFragment : BaseUserFragment() {

    companion object {
        const val FRAGMENT_TAG = "developer_fragment_tag"
        fun newInstance() = DeveloperSettingsFragment()
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SettingsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.developer_settings_fragment, container, false)
        view.navigationIcon.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
        setDeveloperSettings(view)
        return view
    }

    private fun setDeveloperSettings(view: View) {
        view.clearUserPreferences.settingsTitle.text = getString(R.string.developer_settings_clear_user_preferences_title)
        view.clearUserPreferences.settingsDescription.text = getString(R.string.developer_settings_clear_user_preferences_desc)
        view.clearUserPreferences.checkbox.visibility = View.INVISIBLE
        view.clearUserPreferences.setOnClickListener {
            viewModel.clearUserPreferences()
            Snackbar.make(view, "All user preferences cleared", Snackbar.LENGTH_SHORT)
                    .configInformative(context!!, false)
                    .show()
        }

        view.merriamWebsterPreference.settingsTitle.text = "Toggle Merriam-Wesbter state"
        view.merriamWebsterPreference.checkbox.visibility = View.INVISIBLE
        viewModel.getUserLive().observe(this, Observer { user ->
            val state = user.merriamWebsterState
            view.merriamWebsterPreference.settingsDescription.text = when (state) {
                is PluginState.None -> "None"
                is PluginState.FreeTrial -> "Free trial (${if (state.isValid) "valid" else "expired"})"
                is PluginState.Subscribed -> "Subscribed (${if (state.isValid) "valid" else "expired"})"
            }
            view.merriamWebsterPreference.setOnClickListener {
                cycleState(user)
            }
        })
    }

    /**
     * Should cycle through:
     *
     * NONE
     * FREE_TRIAL (valid)
     * FREE_TRIAL (expired)
     * SUBSCRIBED (valid)
     * SUBSCRIBED (expired)
     */
    private fun cycleState(user: User) {
        val state = user.merriamWebsterState
        viewModel.setMerriamWebsterState(
                when {
                    //None -> Free Trial (valid)
                    state is PluginState.None -> PluginState.FreeTrial(user.isAnonymous)
                    //FreeTrial (valid) -> FreeTrial (expired)
                    state is PluginState.FreeTrial && state.isValid -> PluginState.FreeTrial(user.isAnonymous, user.oneDayPastExpiration)
                    //FreeTrial (expired) -> Subscribed (valid)
                    state is PluginState.FreeTrial && !state.isValid -> PluginState.Subscribed()
                    //Subscribed (valid) -> Subscribed (expired)
                    state is PluginState.Subscribed && state.isValid -> PluginState.Subscribed(user.oneDayPastExpiration)
                    //Subscribed (expired) -> FreeTrial (valid)
                    state is PluginState.Subscribed && !state.isValid -> PluginState.FreeTrial(user.isAnonymous)
                    //Default
                    else -> PluginState.None()
                })
    }

}