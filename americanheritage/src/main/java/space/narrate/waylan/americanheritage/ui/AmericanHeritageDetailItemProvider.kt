package space.narrate.waylan.americanheritage.ui

import android.view.ViewGroup
import java.util.Locale
import space.narrate.waylan.americanheritage.databinding.AmericanHeritageItemLayoutBinding
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.ui.widget.DictionaryEntryCardView
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.core.R as coreR


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
) : DetailItemViewHolder(binding.root), DictionaryEntryCardView.PermissionPaneListener {

  init {
    binding.americanHeritageCard.setPermissionPaneListener(this)
  }

  override fun bind(item: DetailItemModel) {
    if (item !is AmericanHeritageModel) return

    binding.run {
      americanHeritageCard.setDictionaryName("American Heritage")
      americanHeritageCard.setStatusLabelForUserAddOn(item.userAddOn)
      americanHeritageCard.setPermission(item.userAddOn)
      americanHeritageCard.setAudio(
        item.audios.map { it.fileUrl }.toList(),
        item.userAddOn,
        listener
      )

      val partOfSpeechMap = item.definitions
        .groupBy { it.partOfSpeech }
        .mapValues { m ->
          m.value.map { it.text.decapitalize(Locale.ENGLISH) }
            .filterNot { it.isEmpty() }
        }
        .toMap()
      americanHeritageCard.setDefinitions(partOfSpeechMap)
    }
  }

  override fun onPermissionDetailsButtonClicked() {
    listener.onAddOnDetailsClicked(AddOn.AMERICAN_HERITAGE)
  }

  override fun onPermissionDismissButtonClicked() {
    listener.onAddOnDismissClicked(AddOn.AMERICAN_HERITAGE)
  }
}