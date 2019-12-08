package space.narrate.waylan.android.ui.details

import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.data.wordset.WordAndMeanings
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemType

class TitleModel(val word: String): DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.TITLE

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is TitleModel) return false
        return word == newOther.word
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is TitleModel) return false
        return word == newOther.word
    }
}


class WordsetModel(val entry: WordAndMeanings): DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.WORDSET

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is WordsetModel) return false
        return this == newOther
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is WordsetModel) return false
        return entry.meanings
            .map { it.def }
            .toTypedArray()
            .contentDeepEquals(
                newOther.entry.meanings
                    .map { it.def }
                    .toTypedArray()
            )
    }
}

class ExamplesModel(val examples: List<Example>): DetailItemModel() {

    override val itemType: DetailItemType = DetailItemType.EXAMPLE

    override fun isSameAs(newOther: DetailItemModel): Boolean {
        if (newOther !is ExamplesModel) return false
        return this == newOther
    }

    override fun isContentSameAs(newOther: DetailItemModel): Boolean {
        // do items point to the same address
        if (newOther !is ExamplesModel) return false
        return examples.toTypedArray().contentDeepEquals(examples.toTypedArray())
    }
}

