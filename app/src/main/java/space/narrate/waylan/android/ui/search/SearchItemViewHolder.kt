package space.narrate.waylan.android.ui.search

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import space.narrate.waylan.android.R
import space.narrate.waylan.core.ui.widget.BannerCardView
import space.narrate.waylan.core.util.AdapterUtils

sealed class SearchItemViewHolder<T : SearchItemModel>(
        val view: View
) : RecyclerView.ViewHolder(view) {

    abstract fun bind(item: T)

    class HeaderViewHolder(
            parent: ViewGroup,
            listener: BannerCardView.Listener
    ) : SearchItemViewHolder<SearchItemModel.HeaderModel>(
            AdapterUtils.inflate(parent, R.layout.list_banner_layout)
    ) {
        private val bannerCardView: BannerCardView = view.findViewById(R.id.banner)

        init {
            bannerCardView.setLisenter(listener)
        }

        override fun bind(item: SearchItemModel.HeaderModel) {
            bannerCardView
                .setText(item.text)
                .setTopButton(item.topButtonText)
                .setBottomButton(item.bottomButtonText)
                .setLabel(item.label)
        }
    }

    class WordViewHolder(
        parent: ViewGroup,
        private val listener: SearchItemAdapter.SearchItemListener
    ) : SearchItemViewHolder<SearchItemModel.WordModel>(
        AdapterUtils.inflate(parent, R.layout.search_word_layout)
    ) {
        private val wordTextView: AppCompatTextView = view.findViewById(R.id.word_text_view)
        private val wordIconView: AppCompatImageView = view.findViewById(R.id.word_icon_image_view)

        override fun bind(item: SearchItemModel.WordModel) {
            view.setOnClickListener { listener.onWordClicked(item) }
            wordTextView.text = item.word.word
            wordIconView.setImageResource(R.drawable.ic_round_search_outlined_24px)
        }
    }

    class UserWordViewHolder(
        parent: ViewGroup,
        private val listener: SearchItemAdapter.SearchItemListener
    ) : SearchItemViewHolder<SearchItemModel.UserWordModel>(
        AdapterUtils.inflate(parent, R.layout.search_word_layout)
    ) {
        private val wordTextView: AppCompatTextView = view.findViewById(R.id.word_text_view)
        private val wordIconView: AppCompatImageView = view.findViewById(R.id.word_icon_image_view)

        override fun bind(item: SearchItemModel.UserWordModel) {
            view.setOnClickListener { listener.onWordClicked(item) }
            wordTextView.text = item.userWord.word
            wordIconView.setImageResource(R.drawable.ic_round_recent_outlined_24px)
        }
    }

    class SuggestViewHolder(
        parent: ViewGroup,
        private val listener: SearchItemAdapter.SearchItemListener
    ) : SearchItemViewHolder<SearchItemModel.SuggestModel>(
        AdapterUtils.inflate(parent, R.layout.search_word_layout)
    ) {
        private val wordTextView: AppCompatTextView = view.findViewById(R.id.word_text_view)
        private val wordIconView: AppCompatImageView = view.findViewById(R.id.word_icon_image_view)

        override fun bind(item: SearchItemModel.SuggestModel) {
            view.setOnClickListener { listener.onWordClicked(item) }
            wordTextView.text = item.suggestItem.term
            wordIconView.setImageResource(R.drawable.ic_round_smart_outlined_24px)
        }
    }

}