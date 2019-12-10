package space.narrate.waylan.settings.ui.thirdparty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.settings.data.ThirdPartyLibrary
import space.narrate.waylan.settings.databinding.ThirdPartyPreferenceListItemBinding

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
        return ThirdPartyLibraryViewHolder(
            ThirdPartyPreferenceListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ThirdPartyLibraryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}