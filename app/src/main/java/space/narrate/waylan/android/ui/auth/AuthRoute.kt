package space.narrate.waylan.android.ui.auth


/**
 * An enumeration that defines how this activity should behave. Use the key
 * [AUTH_ROUTE_EXTRA_KEY] and add an [AuthRoute] to [AuthActivity]'s start intent to
 * determine what [AuthActivity] should expect to accomplish.
 *
 * For example, using [SIGN_UP] will force [AuthActivity] to immediately display the
 * credentials input layout along with both a password and a confirm password field.
 *
 * Note: To allow either log in or sign up in a single layout, a user can switch between
 * the two routes.
 */
enum class AuthRoute {

    /**
     * An [AuthRoute] which checks for a current [FirebaseUser] or creates a new
     * anonymous [FirebaseUser].
     *
     * If a [FirebaseUser] is present, the [space.narrate.waylan.android.data.firestore.users.User]
     * is retreived, [App.setUser] is called and this activity finishes. If there is not a
     * FirebaseUser, a new anonymous user is created, a new
     * [space.narrate.waylan.android.data.firestore.users.User] is created, [App.setUser] is called
     * and this activity finishes.
     *
     * No UI is shown during this route other than the logo
     *
     * TODO handle a poor/no network connection state
     */
    ANONYMOUS,

    /**
     * An [AuthRoute] that allows a user create a new account or tie their
     * existing anonymous account to an email and password.
     *
     * Once credentials are entered, they are checked an either tied to an existing anonymous
     * user or a new user is created. The [space.narrate.waylan.android.data.firestore.users.User]
     * for the [FirebaseUser] is then either updated or created, [App.setUser] is called and
     * this activity finishes.
     */
    SIGN_UP,

    /**
     * An [AuthRoute] that allows a user log in with existing email/password
     * credentials.
     *
     * Once credentials are entered, the user is logged in via [FirebaseAuth], their
     * [space.narrate.waylan.android.data.firestore.users.User] is retreived, [App.setUser] is called
     * and this activity finishes.
     */
    LOG_IN;

    companion object {
        fun fromName(name: String?): AuthRoute {
            return when (name) {
                ANONYMOUS.name -> ANONYMOUS
                SIGN_UP.name -> SIGN_UP
                LOG_IN.name -> LOG_IN
                else -> ANONYMOUS
            }
        }
    }
}