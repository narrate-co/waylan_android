package space.narrate.waylan.android.ui.list

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.android.R
import space.narrate.waylan.android.util.toChip
import space.narrate.waylan.core.data.wordset.Synonym
import space.narrate.waylan.core.ui.widget.BannerCardView
import space.narrate.waylan.core.util.AdapterUtils

sealed class ListItemViewHolder<T : ListItemModel>(val view: View): RecyclerView.ViewHolder(view) {

    abstract fun bind(item: T)

    class HeaderViewHolder(
            parent: ViewGroup,
            listener: BannerCardView.Listener
    ): ListItemViewHolder<ListItemModel.HeaderModel>(
        AdapterUtils.inflate(parent, R.layout.list_banner_layout)
    ) {

        private val bannerCardView: BannerCardView = view.findViewById(R.id.banner)

        init {
            bannerCardView.setLisenter(listener)
        }

        override fun bind(item: ListItemModel.HeaderModel) {
            bannerCardView
                .setText(item.text)
                .setTopButton(item.topButtonText)
                .setBottomButton(item.bottomButtonText)
                .setLabel(item.label)
        }
    }

    class UserWordViewHolder(
            parent: ViewGroup,
            private val listener: ListItemAdapter.ListItemListener
    ): ListItemViewHolder<ListItemModel.UserWordModel>(
        AdapterUtils.inflate(parent, R.layout.list_item_layout)
    ) {

        private val wordTextView: AppCompatTextView = view.findViewById(R.id.word)
        private val partOfSpeechTextView: AppCompatTextView = view.findViewById(R.id.part_of_speech)
        private val definitionTextView: AppCompatTextView = view.findViewById(R.id.definition)
        private val chipGroup: ChipGroup = view.findViewById(R.id.expanded_chip_group)
        private val itemContainer: LinearLayout = view.findViewById(R.id.item_container)

        override fun bind(item: ListItemModel.UserWordModel) {
            wordTextView.text = item.userWord.word

            //Set part of speech
            partOfSpeechTextView.text = item.userWord.partOfSpeechPreview.keys.firstOrNull() ?: ""

            //Set definition
            item.userWord.defPreview.map { it.key }.firstOrNull()?.let {
                definitionTextView.text = it
            }

            //Set synonym chips
            chipGroup.removeAllViews()
            item.userWord.synonymPreview.forEach {syn ->
                val synonym = Synonym(syn.key, OffsetDateTime.now(), OffsetDateTime.now())
                chipGroup.addView(
                        synonym.toChip(view.context, chipGroup) {
                            listener.onWordClicked(it.synonym)
                        }
                )
            }

            itemContainer.setOnClickListener {
                listener.onWordClicked(item.userWord.word)
            }
        }
    }

    class GlobalWordViewHolder(
            parent: ViewGroup,
            private val listener: ListItemAdapter.ListItemListener
    ) : ListItemViewHolder<ListItemModel.GlobalWordModel>(
            AdapterUtils.inflate(parent, R.layout.list_item_layout)
    ) {

        private val wordTextView: AppCompatTextView = view.findViewById(R.id.word)
        private val partOfSpeechTextView: AppCompatTextView = view.findViewById(R.id.part_of_speech)
        private val definitionTextView: AppCompatTextView = view.findViewById(R.id.definition)
        private val chipGroup: ChipGroup = view.findViewById(R.id.expanded_chip_group)
        private val itemContainer: LinearLayout = view.findViewById(R.id.item_container)

        override fun bind(item: ListItemModel.GlobalWordModel) {
            wordTextView.text = item.globalWord.word

            //Set part of speech
            partOfSpeechTextView.text = item.globalWord.partOfSpeechPreview.keys.firstOrNull() ?: ""

            //Set definition
            item.globalWord.defPreview.map { it.key }.firstOrNull()?.let {
                definitionTextView.text = it
            }

            //Set synonym chips
            chipGroup.removeAllViews()
            item.globalWord.synonymPreview.forEach {
                val synonym = Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now())
                chipGroup.addView(
                        synonym.toChip(view.context, chipGroup) {
                            listener.onWordClicked(it.synonym)
                        }
                )
            }

            itemContainer.setOnClickListener {
                listener.onWordClicked(item.globalWord.word)
            }
        }
    }
}
