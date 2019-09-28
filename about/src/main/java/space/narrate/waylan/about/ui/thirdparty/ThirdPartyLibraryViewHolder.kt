package space.narrate.waylan.about.ui.thirdparty

import androidx.recyclerview.widget.RecyclerView
import space.narrate.waylan.about.data.ThirdPartyLibrary
import space.narrate.waylan.about.databinding.ThirdPartyPreferenceListItemBinding

/**
 * A simple ViewHolder to hold and bind a [ThirdPartyLibrary] to a RecyclerView item view.
 */
class ThirdPartyLibraryViewHolder(
    private val binding: ThirdPartyPreferenceListItemBinding,
    private val listener: ThirdPartyLibraryAdapter.Listener
): RecyclerView.ViewHolder(binding.root) {

    fun bind(lib: ThirdPartyLibrary) {
        binding.run {
            root.setOnClickListener { listener.onClick(lib) }
            preference.setTitle(lib.name)
            preference.setDesc(lib.url)
        }
    }
}