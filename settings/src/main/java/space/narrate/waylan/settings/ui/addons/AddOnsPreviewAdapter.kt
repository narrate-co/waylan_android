package space.narrate.waylan.settings.ui.addons

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.settings.databinding.AddOnPreviewItemBinding

class AddOnsPreviewAdapter : ListAdapter<AddOnItemModel, AddOnViewPreviewHolder>(
    AdapterUtils.diffableItemCallback()
) {

    fun getItemAt(position: Int): AddOnItemModel = getItem(position)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddOnViewPreviewHolder {
        return AddOnViewPreviewHolder(
            AddOnPreviewItemBinding.inflate(parent.inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddOnViewPreviewHolder, position: Int) {
        holder.onBind(getItemAt(position))
    }

}