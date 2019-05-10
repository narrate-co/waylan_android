package space.narrate.words.android.ui.list

import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.firestore.words.GlobalWord
import space.narrate.words.android.ui.common.Diffable

sealed class ListItemModel : Diffable<ListItemModel> {

    class HeaderModel(
            val text: Int,
            val topButtonText: Int? = null,
            val bottomButtonText: Int? = null,
            val label: Int? = null
    ) : ListItemModel() {
        override fun isSameAs(newOther: ListItemModel): Boolean {
            return newOther is HeaderModel
        }

        override fun isContentSameAs(newOther: ListItemModel): Boolean {
            if (newOther !is HeaderModel) return false
            return text == newOther.text &&
                topButtonText == newOther.topButtonText &&
                bottomButtonText == newOther.bottomButtonText &&
                label == newOther.label
        }
    }

    class UserWordModel(val userWord: UserWord) : ListItemModel() {
        override fun isSameAs(newOther: ListItemModel): Boolean {
            if (newOther !is UserWordModel) return false
            return userWord.id == newOther.userWord.id
        }

        override fun isContentSameAs(newOther: ListItemModel): Boolean {
            if (newOther !is UserWordModel) return false
            return userWord.word == newOther.userWord.word &&
                    userWord.modified == newOther.userWord.modified &&
                    userWord.types == newOther.userWord.types &&
                    userWord.partOfSpeechPreview == newOther.userWord.partOfSpeechPreview &&
                    userWord.defPreview == newOther.userWord.defPreview &&
                    userWord.synonymPreview == newOther.userWord.synonymPreview &&
                    userWord.labelsPreview == newOther.userWord.labelsPreview
        }
    }

    class GlobalWordModel(val globalWord: GlobalWord) : ListItemModel() {
        override fun isSameAs(newOther: ListItemModel): Boolean {
            if (newOther !is GlobalWordModel) return false
            return globalWord.id == newOther.globalWord.id
        }

        override fun isContentSameAs(newOther: ListItemModel): Boolean {
            if (newOther !is GlobalWordModel) return false
            return globalWord.word == newOther.globalWord.word &&
                    globalWord.modified == newOther.globalWord.modified &&
                    globalWord.partOfSpeechPreview == newOther.globalWord.partOfSpeechPreview &&
                    globalWord.defPreview == newOther.globalWord.defPreview &&
                    globalWord.synonymPreview == newOther.globalWord.synonymPreview &&
                    globalWord.labelsPreview == newOther.globalWord.labelsPreview
        }
    }
}