package space.narrate.waylan.core.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.navigation.NavDestination
import space.narrate.waylan.core.ui.common.Event

interface Navigator {

    enum class BackType {
        ICON, DRAG
    }

    val currentDestination: LiveData<Destination>
    val shouldNavigateBack: LiveData<Event<Boolean>>

    fun setCurrentDestination(dest: NavDestination, arguments: Bundle?)
    fun toBack(backType: BackType, from: String): Boolean
    fun toHome(context: Context, clearStack: Boolean, filterIntent: Intent?)
    fun toAuth(context: Context, filterIntent: Intent? = null)
    fun toLogIn(context: Context, filterIntent: Intent? = null)
    fun toSignUp(context: Context, filterIntent: Intent? = null)
}