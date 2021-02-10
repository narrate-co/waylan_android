package space.narrate.waylan.test_common

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class RecyclerViewMatcher(private val recyclerViewId: Int) {

    /**
     * Find the itemView of a ViewHolder inside a RecyclerView.
     */
    fun atPosition(pos: Int): Matcher<View> {
        return atPositionOnView(pos)
    }

    /**
     * When [viewId] is -1, find the itemView of a ViewHolder inside this matcher's RecyclerView.
     * When [viewId] is not -1, ind a child view of an itemView of a ViewHolder inside a
     * RecyclerView
     */
    fun atPositionOnView(pos: Int, viewId: Int = -1): Matcher<View> {
        return object : TypeSafeMatcher<View>() {

            private var resources: Resources? = null
            private var childView: View? = null

            override fun describeTo(description: Description) {
                var idDesc = recyclerViewId.toString()
                if (resources != null) {
                    idDesc = try {
                        resources?.getResourceName(recyclerViewId) ?: idDesc
                    } catch (e: Exception) {
                        "$recyclerViewId (resource name not found)"
                    }
                }

                description.appendText("with id: $idDesc")
            }

            override fun matchesSafely(item: View): Boolean {
                resources = item.resources

                // Find the itemView of the adapter position requested.
                if (childView == null) {
                    val recyclerView = item.rootView.findViewById<RecyclerView>(recyclerViewId)
                    if (recyclerView != null && recyclerView.id == recyclerViewId) {
                        childView = recyclerView.findViewHolderForAdapterPosition(pos)?.itemView
                    } else {
                        return false
                    }
                }

                // If no viewId is given, return whether the found adapter position itemView
                // matches. Otherwise, find a child view of the found adapter position and
                // return whether that view matches.
                return if (viewId == -1) {
                    item == childView
                } else {
                    val targetView = childView?.findViewById<View>(viewId)
                    item == targetView
                }
            }

        }
    }
}

