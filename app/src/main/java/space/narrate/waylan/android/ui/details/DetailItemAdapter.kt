package space.narrate.waylan.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import space.narrate.waylan.android.util.AdapterUtils
import java.lang.IllegalArgumentException

class DetailItemAdapter(
    private val listener: Listener
) : ListAdapter<DetailItemModel, DetailItemViewHolder<DetailItemModel>>(
    AdapterUtils.diffableItemCallback()
) {

    interface Listener : MerriamWebsterCardView.Listener {
        fun onSynonymChipClicked(synonym: String)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is DetailItemModel.TitleModel -> VIEW_TYPE_TITLE
        is DetailItemModel.MerriamWebsterModel -> VIEW_TYPE_MERRIAM_WEBSTER
        is DetailItemModel.WordsetModel -> VIEW_TYPE_WORDSET
        is DetailItemModel.ExamplesModel -> VIEW_TYPE_EXAMPLE
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailItemViewHolder<DetailItemModel> {
        return when (viewType) {
            VIEW_TYPE_TITLE ->
                DetailItemViewHolder.TitleViewHolder(parent)
            VIEW_TYPE_MERRIAM_WEBSTER ->
                DetailItemViewHolder.MerriamWebsterViewHolder(parent, listener)
            VIEW_TYPE_WORDSET ->
                DetailItemViewHolder.WordsetViewHolder(parent, listener)
            VIEW_TYPE_EXAMPLE ->
                DetailItemViewHolder.ExamplesViewHolder(parent, listener)
            else ->
                throw IllegalArgumentException("Unsupported viewType being inflated - $viewType")
        } as DetailItemViewHolder<DetailItemModel>
    }

    override fun onBindViewHolder(
        holder: DetailItemViewHolder<DetailItemModel>,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    companion object {
        const val VIEW_TYPE_TITLE = 1
        const val VIEW_TYPE_MERRIAM_WEBSTER = 2
        const val VIEW_TYPE_WORDSET = 3
        const val VIEW_TYPE_EXAMPLE = 4
    }
}