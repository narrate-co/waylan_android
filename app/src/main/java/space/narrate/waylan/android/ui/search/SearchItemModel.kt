package space.narrate.waylan.android.ui.search

import space.narrate.waylan.core.data.wordset.Word
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.spell.SuggestItem
import space.narrate.waylan.core.ui.common.Diffable

sealed class SearchItemModel : Diffable<SearchItemModel> {

    class HeaderModel(
            val text: Int,
            val topButtonText: Int? = null,
            val bottomButtonText: Int? = null,
            val label: Int? = null
    ) : SearchItemModel() {
        override fun isSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is HeaderModel) return false
            return this == newOther
        }

        override fun isContentSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is HeaderModel) return false
            return text == newOther.text &&
                topButtonText == newOther.topButtonText &&
                bottomButtonText == newOther.bottomButtonText &&
                label == newOther.label
        }
    }

    class WordModel(val word: Word) : SearchItemModel() {
        override fun isSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is WordModel) return false
            return word.word == newOther.word.word
        }

        override fun isContentSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is WordModel) return false
            return word.word == newOther.word.word
        }

        override fun getChangePayload(newOther: SearchItemModel): Any? = null
    }

    class UserWordModel(val userWord: UserWord) : SearchItemModel() {
        override fun isSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is UserWordModel) return false
            return userWord.id == newOther.userWord.id
        }

        override fun isContentSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is UserWordModel) return false
            return userWord.word == newOther.userWord.word
        }
    }

    class SuggestModel(val suggestItem: SuggestItem) : SearchItemModel() {
        override fun isSameAs(newOther: SearchItemModel): Boolean {
            return this == newOther
        }

        override fun isContentSameAs(newOther: SearchItemModel): Boolean {
            if (newOther !is SuggestModel) return false
            return suggestItem.term == newOther.suggestItem.term &&
                    suggestItem.distance == newOther.suggestItem.distance &&
                    suggestItem.count == newOther.suggestItem.count
        }
    }
}