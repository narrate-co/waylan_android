package com.words.android.ui.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.words.android.App
import com.words.android.Navigator


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