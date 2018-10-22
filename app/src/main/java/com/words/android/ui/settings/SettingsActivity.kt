package com.words.android.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import com.words.android.App
import com.words.android.MainActivity
import com.words.android.Navigator
import com.words.android.R
import com.words.android.ui.common.BaseUserActivity

class SettingsActivity : BaseUserActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            showSettings()
        }
    }

    private fun showSettings() = Navigator.showSettings(this)

    fun showAbout() = Navigator.showAbout(this)

    fun restartWithReconstructedStack() {
        val mainIntent = Intent(this, MainActivity::class.java)
        val taskBuilder = TaskStackBuilder.create(this)
        taskBuilder.addParentStack(MainActivity::class.java)
        taskBuilder.addNextIntent(mainIntent)

        val settingsIntent = Intent(this, SettingsActivity::class.java)
        taskBuilder.addNextIntent(settingsIntent)

        taskBuilder.startActivities()

    }

}
