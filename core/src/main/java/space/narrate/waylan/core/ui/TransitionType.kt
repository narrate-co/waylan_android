package space.narrate.waylan.core.ui

/**
 * An enumeration of different modes of animating between screens.
 */
enum class TransitionType {
  /**
   * No transition should be applied. This is primarily valuable when a screen is part of the
   * launcher activity and is being displayed for the first time.
   */
  NONE,

  /**
   * Fade through is used when two screens are transitioning but do not share a spacial
   * relationship.
   */
  FADE_THROUGH,

  /**
   * Shared axis X transition should be used to show a spacial relationship between two screens
   * in the horizontal axis.
   */
  SHARED_AXIS_X,

  /**
   * Shared axis Y should be used to show a spacial relationship between two screens in the
   * vertical direction
   */

  SHARED_AXIS_Y,
  /**
   * Container transform is reserved for hero moments when two screens share content.
   */
  CONTAINER_TRANSFORM
}