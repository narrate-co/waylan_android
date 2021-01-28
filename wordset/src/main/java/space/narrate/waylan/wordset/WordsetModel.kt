package space.narrate.waylan.wordset

import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.data.wordset.WordAndMeanings
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

/**
 * Wordset's [DetailItemModel] which is used to construct a DetailItemViewHolder that is able to
 * be displayed by the details screen.
 */
class WordsetModel(
  val wordAndMeanings: WordAndMeanings,
  val examples: List<Example>
) : DetailItemModel() {

  override val itemType: DetailItemType = DetailItemType.WORDSET

  override fun isSameAs(newOther: DetailItemModel): Boolean {
    if (newOther !is WordsetModel) return false
    return  wordAndMeanings == newOther.wordAndMeanings && examples == newOther.examples
  }

  override fun isContentSameAs(newOther: DetailItemModel): Boolean {
    if (newOther !is WordsetModel) return false

    return wordAndMeanings.word == newOther.wordAndMeanings.word
      && wordAndMeanings.meanings
        .toTypedArray()
        .contentDeepEquals(newOther.wordAndMeanings.meanings.toTypedArray())
      && examples.toTypedArray().contentDeepEquals(newOther.examples.toTypedArray())
  }
}