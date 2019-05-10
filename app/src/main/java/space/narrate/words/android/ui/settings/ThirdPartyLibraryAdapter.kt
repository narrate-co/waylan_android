package space.narrate.words.android.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.words.android.R
import space.narrate.words.android.util.AdapterUtils

class ThirdPartyLibraryAdapter(
    private val listener: Listener
) : ListAdapter<ThirdPartyLibrary, ThirdPartyLibraryViewHolder>(
    AdapterUtils.emptyDiffItemCallback()
) {

    interface Listener {
        fun onClick(lib: ThirdPartyLibrary)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThirdPartyLibraryViewHolder {
        return ThirdPartyLibraryViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.settings_check_preference_list_item, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ThirdPartyLibraryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}