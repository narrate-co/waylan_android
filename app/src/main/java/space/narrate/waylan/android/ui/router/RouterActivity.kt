package space.narrate.waylan.android.ui.router

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.koin.android.ext.android.inject
import space.narrate.waylan.android.Navigator
import space.narrate.waylan.android.data.auth.AuthenticationStore


/**
 * A Theme.NoDisplay Activity that serves as an entry hub for all intent filters which Words
 * is able to handle.
 *
 * For an informative note on Theme.NoDisplay see
 * <a>https://plus.google.com/+DianneHackborn/posts/LjnRzJKWPGW</a>
 */
class RouterActivity : Activity() {

    private val authenticationStore: AuthenticationStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We should only get to RouterActivity by receiving a filtered intent
        // defined in AndroidManifest.xml
        if (intent == null) {
            // Default to AuthActivity
            Navigator.launchAuth(this)
            finish()
            return
        }

        val hasProcessText = intent.hasExtra(Intent.EXTRA_PROCESS_TEXT)
        // If we're processing textRes, make sure there's a valid user
        if (hasProcessText) {
            if (authenticationStore.hasUser) {
                // Go straight to MainActivity and pass along intent to be processed
                Navigator.launchMain(this, true, intent)
            } else {
                // Go to AuthActivity, authorize, and then MainActivity,
                // passing along intent to each
                Navigator.launchAuth(this, null, intent)
            }
        } else {
            Navigator.launchAuth(this, null, intent)
        }

        finish()
    }


}