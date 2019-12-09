package space.narrate.waylan.core.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.navigation.NavDestination
import space.narrate.waylan.core.data.auth.AuthRoute
import space.narrate.waylan.core.ui.common.Event

interface Navigator {
    enum class BackType {
        ICON, DRAG
    }
    val currentDestination: LiveData<Destination>
    val shouldNavigateBack: LiveData<Event<Boolean>>
    fun back(backType: BackType, from: String): Boolean
    fun launchMain(context: Context, clearStack: Boolean, filterIntent: Intent?)
    fun launchAuth(context: Context, authRoute: AuthRoute? = null, filterIntent: Intent? = null)
    fun setCurrentDestination(dest: NavDestination, arguments: Bundle?)
}