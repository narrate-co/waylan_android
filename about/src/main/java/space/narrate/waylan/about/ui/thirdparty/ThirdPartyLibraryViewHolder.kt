package space.narrate.waylan.about.ui.thirdparty

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import space.narrate.waylan.android.R
import space.narrate.waylan.android.data.prefs.ThirdPartyLibrary
import space.narrate.waylan.android.ui.widget.CheckPreferenceView

/**
 * A simple ViewHolder to hold and bind a [ThirdPartyLibrary] to a RecyclerView item view.
 */
class ThirdPartyLibraryViewHolder(
        private val view: View,
        private val listener: ThirdPartyLibraryAdapter.Listener
): RecyclerView.ViewHolder(view) {

    private val preferenceView: CheckPreferenceView = view.findViewById(R.id.preference)

    fun bind(lib: ThirdPartyLibrary) {
        view.setOnClickListener { listener.onClick(lib) }
        preferenceView.setTitle(lib.name)
        preferenceView.setDesc(lib.url)
    }
}