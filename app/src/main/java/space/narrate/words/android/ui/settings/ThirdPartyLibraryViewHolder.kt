package space.narrate.words.android.ui.settings

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.settings_check_preference_list_item.view.*

/**
 * A simple ViewHolder to hold and bind a [ThirdPartyLibrary] to a RecyclerView item view.
 */
class ThirdPartyLibraryViewHolder(
        private val view: View,
        private val listener: ThirdPartyListener
): RecyclerView.ViewHolder(view) {

    interface ThirdPartyListener {
        fun onClick(lib: ThirdPartyLibrary)
    }

    fun bind(lib: ThirdPartyLibrary) {
        view.preference.setTitle(lib.name)
        view.preference.setDesc(lib.url)
        view.setOnClickListener { listener.onClick(lib) }
    }
}