package space.narrate.waylan.core.data.firestore.users

/**
 * An enumeration of flags that denote the privacy level of a user-created piece of content.
 *
 * @property PRIVATE is content that is only visible to the user who created it.
 * @property PUBLIC is content visible to anyone on Waylan.
 */
enum class ContentVisibility {
  PRIVATE, PUBLIC
}