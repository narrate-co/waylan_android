package space.narrate.waylan.settings.ui.addons

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.core.util.AdapterUtils
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.settings.databinding.AddOnPreviewItemBinding

/**
 * An adapter that shows a preview of what each add-ons looks like.
 */
class AddOnsPreviewAdapter : ListAdapter<AddOnItemModel, AddOnViewPreviewHolder>(
    AdapterUtils.diffableItemCallback()
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddOnViewPreviewHolder {
        return AddOnViewPreviewHolder(
            AddOnPreviewItemBinding.inflate(parent.inflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AddOnViewPreviewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

}