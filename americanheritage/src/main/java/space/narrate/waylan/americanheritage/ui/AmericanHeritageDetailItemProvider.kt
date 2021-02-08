package space.narrate.waylan.americanheritage.ui

import android.view.ViewGroup
import java.util.Locale
import space.narrate.waylan.americanheritage.databinding.AmericanHeritageItemLayoutBinding
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.util.inflater

class AmericanHeritageDetailItemProvider : DetailItemProvider {

  override val itemType: DetailItemType = DetailItemType.AMERICAN_HERITAGE

  override fun createViewHolder(
    parent: ViewGroup,
    listener: DetailAdapterListener
  ): DetailItemViewHolder {
    return AmericanHeritageViewHolder(
      AmericanHeritageItemLayoutBinding.inflate(parent.inflater, parent, false),
      listener
    )
  }
}

class AmericanHeritageViewHolder(
  private val binding: AmericanHeritageItemLayoutBinding,
  private val listener: DetailAdapterListener
) : DetailItemViewHolder(binding.root) {

  override fun bind(item: DetailItemModel) {
    if (item !is AmericanHeritageModel) return

    binding.run {
      americanHeritageCard.setDictionaryName("American Heritage")
      // TODO: Set add-on status label
      americanHeritageCard.setStatusLabel(null)

      val partOfSpeechMap = item.definitions
        .groupBy { it.partOfSpeech }
        .mapValues { m -> m.value.map { it.text.decapitalize(Locale.ENGLISH) } }
        .toMap()
      americanHeritageCard.setDefinitions(partOfSpeechMap)
    }
  }
}