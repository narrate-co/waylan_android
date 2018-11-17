package com.wordsdict.android.ui.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.wordsdict.android.App
import com.wordsdict.android.Navigator


class RouterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent == null) {
            Navigator.launchAuth(this)
            finish()
            return
        }

        val hasProcessText = intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)
        if (hasProcessText) {
            if ((application as App).hasUser) {
                Navigator.launchMain(this, true, intent)
            } else {
                Navigator.launchAuth(this, null, intent)
            }
        } else {
            Navigator.launchAuth(this, null, intent)
        }

        finish()
    }


}