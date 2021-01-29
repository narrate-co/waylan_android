package space.narrate.waylan.android.ui.list

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import org.threeten.bp.OffsetDateTime
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.ListBannerLayoutBinding
import space.narrate.waylan.android.databinding.ListItemLayoutBinding
import space.narrate.waylan.android.util.toChip
import space.narrate.waylan.core.data.wordset.Synonym
import space.narrate.waylan.core.ui.widget.BannerCardView
import space.narrate.waylan.core.util.AdapterUtils

sealed class ListItemViewHolder<T : ListItemModel>(val view: View): RecyclerView.ViewHolder(view) {

  abstract fun bind(item: T)

  class HeaderViewHolder(
    private val binding: ListBannerLayoutBinding,
    listener: BannerCardView.Listener
  ): ListItemViewHolder<ListItemModel.HeaderModel>(binding.root) {

    init {
      binding.banner.setLisenter(listener)
    }

    override fun bind(item: ListItemModel.HeaderModel) {
      binding.banner
        .setText(item.text)
        .setTopButton(item.topButtonText)
        .setBottomButton(item.bottomButtonText)
        .setLabel(item.label)
    }
  }

  class UserWordViewHolder(
    private val binding: ListItemLayoutBinding,
    private val listener: ListItemAdapter.ListItemListener
  ): ListItemViewHolder<ListItemModel.UserWordModel>(binding.root) {

    override fun bind(item: ListItemModel.UserWordModel) {
      binding.run {
        itemContainer.transitionName = item.userWord.id
        word.text = item.userWord.word

        //Set part of speech
        partOfSpeech.text = item.userWord.partOfSpeechPreview.keys.firstOrNull() ?: ""

        //Set definition
        item.userWord.defPreview.map { it.key }.firstOrNull()?.let {
          definition.text = it
        }

        //Set synonym chips
        expandedChipGroup.removeAllViews()
        item.userWord.synonymPreview.forEach {syn ->
          val synonym = Synonym(syn.key, OffsetDateTime.now(), OffsetDateTime.now())
          expandedChipGroup.addView(
            synonym.toChip(view.context, expandedChipGroup) {
              listener.onWordClicked(it.synonym, binding.root)
            }
          )
        }

        itemContainer.setOnClickListener {
          listener.onWordClicked(item.userWord.word, binding.root)
        }
      }

    }
  }

  class GlobalWordViewHolder(
    private val binding: ListItemLayoutBinding,
    private val listener: ListItemAdapter.ListItemListener
  ) : ListItemViewHolder<ListItemModel.GlobalWordModel>(binding.root) {

    override fun bind(item: ListItemModel.GlobalWordModel) {
      binding.run {
        word.text = item.globalWord.word

        //Set part of speech
        partOfSpeech.text = item.globalWord.partOfSpeechPreview.keys.firstOrNull() ?: ""

        //Set definition
        item.globalWord.defPreview.map { it.key }.firstOrNull()?.let {
          definition.text = it
        }

        //Set synonym chips
        expandedChipGroup.removeAllViews()
        item.globalWord.synonymPreview.forEach {
          val synonym = Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now())
          expandedChipGroup.addView(
            synonym.toChip(view.context, expandedChipGroup) {
              listener.onWordClicked(it.synonym, binding.root)
            }
          )
        }

        itemContainer.setOnClickListener {
          listener.onWordClicked(item.globalWord.word, binding.root)
        }
      }

    }
  }
}
