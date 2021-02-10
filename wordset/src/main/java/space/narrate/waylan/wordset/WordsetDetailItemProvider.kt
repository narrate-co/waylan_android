package space.narrate.waylan.wordset

import android.view.ViewGroup
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.ui.widget.DictionaryEntryCardView
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.wordset.databinding.WordsetItemLayoutBinding

class WordsetDetailItemProvider : DetailItemProvider {

  override val itemType: DetailItemType = DetailItemType.WORDSET

  override fun createViewHolder(
    parent: ViewGroup,
    listener: DetailAdapterListener
  ): DetailItemViewHolder {
    return WordsetViewHolder(
      WordsetItemLayoutBinding.inflate(parent.inflater, parent, false),
      listener
    )
  }
}

class WordsetViewHolder(
  private val binding: WordsetItemLayoutBinding,
  private val listener: DetailAdapterListener
) : DetailItemViewHolder(binding.root), DictionaryEntryCardView.DictionaryEntryListener {

  init {
    binding.wordsetCard.setListener(this)
  }

  override fun bind(item: DetailItemModel) {
    if (item !is WordsetModel) return
    binding.run {
      wordsetCard.setDictionaryName("Wordset")
      wordsetCard.setStatusLabel(null)

      val partOfSpeechEntryMap = item.wordAndMeanings
        .meanings
        .groupBy { it.partOfSpeech }
        .mapValues { m -> m.value.map { it.def } }
        .toMap()
      wordsetCard.setDefinitions(partOfSpeechEntryMap)

      val examples = item.examples.map { it.example }
      wordsetCard.setExamples(examples)

      val relatedWords = item.wordAndMeanings
        .meanings
        .map { it.synonyms }
        .flatten()
        .map { it.synonym }
      wordsetCard.setRelatedWords(relatedWords)
    }
  }

  override fun onRelatedWordClicked(word: String) {
    listener.onSynonymChipClicked(word)
  }
}