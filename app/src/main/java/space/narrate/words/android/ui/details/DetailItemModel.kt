package space.narrate.words.android.ui.details

import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.disk.wordset.Example
import space.narrate.words.android.data.disk.wordset.WordAndMeanings
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState
import space.narrate.words.android.ui.common.Diffable

sealed class DetailItemModel : Diffable<DetailItemModel> {

    class TitleModel(val word: String): DetailItemModel() {
        override fun isSameAs(newOther: DetailItemModel): Boolean {
            if (newOther !is TitleModel) return false
            return word == newOther.word
        }

        override fun isContentSameAs(newOther: DetailItemModel): Boolean {
            if (newOther !is TitleModel) return false
            return word == newOther.word
        }
    }

    class MerriamWebsterModel(
        val entries: List<MwWordAndDefinitionGroups>,
        val user: User?
    ) : DetailItemModel() {
        override fun isSameAs(newOther: DetailItemModel): Boolean {
            if (newOther !is MerriamWebsterModel) return false
            return entries == newOther.entries && user == newOther.user
        }

        override fun isContentSameAs(newOther: DetailItemModel): Boolean {
            // do items point to the same address
            if (newOther !is MerriamWebsterModel) return false

            return entries.toTypedArray().contentDeepEquals(newOther.entries.toTypedArray()) &&
                user?.merriamWebsterState == newOther.user?.merriamWebsterState &&
                user?.merriamWebsterState?.remainingDays ==
                newOther.user?.merriamWebsterState?.remainingDays
        }
    }

    class WordsetModel(val entry: WordAndMeanings): DetailItemModel() {
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
}

