package space.narrate.waylan.core.details

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A ViewHolder which can be displayed by :app's DetailItemAdpater. Any module which wishes to
 * display data in a word's details screen should implement a [DetailItemModel] and its accompanying
 * [DetailItemViewHolder].
 */
abstract class DetailItemViewHolder(
    val view: View
): RecyclerView.ViewHolder(view) {
    open fun bind(item: DetailItemModel) { }
}