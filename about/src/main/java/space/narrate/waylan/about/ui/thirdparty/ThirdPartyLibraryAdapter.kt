package space.narrate.waylan.about.ui.thirdparty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.about.data.ThirdPartyLibrary
import space.narrate.waylan.about.databinding.ThirdPartyPreferenceListItemBinding
import space.narrate.waylan.core.util.AdapterUtils

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