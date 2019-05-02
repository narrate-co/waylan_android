package space.narrate.words.android.ui.details

/**
 * A sealed class of all item types to be shown by [MerriamWebsterListAdapter].
 */
sealed class MerriamWebsterListItem(val id: String) : Diffable<MerriamWebsterListItem> {

    class PartOfSpeech(id: String, val partOfSpeech: String) : MerriamWebsterListItem(id) {
        override fun isSameAs(newOther: MerriamWebsterListItem): Boolean {
            return this.id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterListItem): Boolean {
            if (newOther !is PartOfSpeech) return false

            return partOfSpeech == newOther.partOfSpeech
        }

        override fun getChangePayload(newOther: MerriamWebsterListItem): Any? = null
    }

    class Definition(id: String, val def: String) : MerriamWebsterListItem(id) {
        override fun isSameAs(newOther: MerriamWebsterListItem): Boolean {
            return id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterListItem): Boolean {
            if (newOther !is Definition) return false

            return def == newOther.def
        }

        override fun getChangePayload(newOther: MerriamWebsterListItem): Any? = null
    }

    class Related(id: String, val words: List<String>) : MerriamWebsterListItem(id) {
        override fun isSameAs(newOther: MerriamWebsterListItem): Boolean {
            return id == newOther.id
        }

        override fun isContentSameAs(newOther: MerriamWebsterListItem): Boolean {
            if (newOther !is Related) return false

            return words.containsAll(newOther.words)
        }

        override fun getChangePayload(newOther: MerriamWebsterListItem): Any? {
            return null
        }
    }

    class PermissionPane : MerriamWebsterListItem("permission_pane_id") {
        override fun isSameAs(newOther: MerriamWebsterListItem): Boolean {
            return id == id
        }

        override fun isContentSameAs(newOther: MerriamWebsterListItem): Boolean {
            return true
        }

        override fun getChangePayload(newOther: MerriamWebsterListItem): Any? = null
    }
}