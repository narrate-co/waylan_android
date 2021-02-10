package space.narrate.waylan.core.data.firestore.users

import java.util.Date

/**
 * A Firestore document to hold single, mutable example that is associated with a [UserWord],
 * created by a user.
 */
data class UserWordExample(
  var id: String = "",
  var example: String = "",
  var created: Date = Date(),
  var modified: Date = Date(),
  var visibility: String = ContentVisibility.PRIVATE.name
)