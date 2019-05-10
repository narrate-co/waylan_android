package space.narrate.words.android.ui.details

import space.narrate.words.android.ui.common.Diffable

/**
 * A sealed class of all item types to be shown by [MerriamWebsterItemAdapter].
 */
sealed class MerriamWebsterItemModel(val id: String) : Diffable<MerriamWebsterItemModel> {

    class PartOfSpeechModel(id: String, val partOfSpeech: String) : MerriamWebsterItemModel(id) {
        override fun isSameAs(newOther: MerriamWebsterItemModel): Boolean {
            return this.id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterItemModel): Boolean {
            if (newOther !is PartOfSpeechModel) return false
            return partOfSpeech == newOther.partOfSpeech
        }
    }

    class DefinitionModel(id: String, val def: String) : MerriamWebsterItemModel(id) {
        override fun isSameAs(newOther: MerriamWebsterItemModel): Boolean {
            return id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterItemModel): Boolean {
            if (newOther !is DefinitionModel) return false
            return def == newOther.def
        }
    }

    class RelatedModel(id: String, val words: List<String>) : MerriamWebsterItemModel(id) {
        override fun isSameAs(newOther: MerriamWebsterItemModel): Boolean {
            return id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterItemModel): Boolean {
            if (newOther !is RelatedModel) return false
            return words.containsAll(newOther.words)
        }
    }

    class PermissionPaneModel : MerriamWebsterItemModel("permission_pane_id") {
        override fun isSameAs(newOther: MerriamWebsterItemModel): Boolean {
            return id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterItemModel): Boolean {
            if (newOther !is PermissionPaneModel) return false
            return id == newOther.id
        }
    }
}