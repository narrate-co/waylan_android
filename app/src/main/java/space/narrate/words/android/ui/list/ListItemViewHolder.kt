package space.narrate.words.android.ui.list

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import space.narrate.words.android.data.disk.wordset.Synonym
import space.narrate.words.android.util.*
import kotlinx.android.synthetic.main.list_item_layout.view.*
import org.threeten.bp.OffsetDateTime
import space.narrate.words.android.R
import space.narrate.words.android.util.widget.BannerCardView

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

        override fun bind(item: ListItemModel.UserWordModel) {
            wordTextView.text = item.userWord.word

            //Set part of speech
            partOfSpeechTextView.text = item.userWord.partOfSpeechPreview.keys.first()

            //Set definition
            item.userWord.defPreview.map { it.key }.firstOrNull()?.let {
                definitionTextView.text = it
            }

            //Set synonym chips
            chipGroup.removeAllViews()
            item.userWord.synonymPreview.forEach {
                val synonym = Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now())
                chipGroup.addView(
                        synonym.toChip(view.context, view.expanded_chip_group) {
                            listener.onWordClicked(it.synonym)
                        }
                )
            }

            view.item_container.setOnClickListener {
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

        override fun bind(item: ListItemModel.GlobalWordModel) {
            wordTextView.text = item.globalWord.word

            //Set part of speech
            partOfSpeechTextView.text = item.globalWord.partOfSpeechPreview.keys.first()

            //Set definition
            item.globalWord.defPreview.map { it.key }.firstOrNull()?.let {
                definitionTextView.text = it
            }

            //Set synonym chips
            chipGroup.removeAllViews()
            item.globalWord.synonymPreview.forEach {
                val synonym = Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now())
                chipGroup.addView(
                        synonym.toChip(view.context, view.expanded_chip_group) {
                            listener.onWordClicked(it.synonym)
                        }
                )
            }

            view.item_container.setOnClickListener {
                listener.onWordClicked(item.globalWord.word)
            }
        }
    }
}
