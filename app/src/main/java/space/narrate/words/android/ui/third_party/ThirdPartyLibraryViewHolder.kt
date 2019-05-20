package space.narrate.words.android.ui.third_party

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import space.narrate.words.android.R
import space.narrate.words.android.data.prefs.ThirdPartyLibrary
import space.narrate.words.android.util.widget.CheckPreferenceView

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