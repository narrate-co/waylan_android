package space.narrate.words.android.ui.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import space.narrate.words.android.data.disk.wordset.Synonym
import space.narrate.words.android.data.repository.FirestoreGlobalSource
import space.narrate.words.android.data.repository.FirestoreUserSource
import space.narrate.words.android.data.repository.WordSource
import space.narrate.words.android.ui.common.HeaderBanner
import space.narrate.words.android.ui.common.HeaderBannerListener
import space.narrate.words.android.ui.common.HeaderBannerBinder
import space.narrate.words.android.util.*
import kotlinx.android.synthetic.main.list_banner_layout.view.*
import kotlinx.android.synthetic.main.list_item_layout.view.*
import org.threeten.bp.OffsetDateTime

/**
 * A sealed class to be used to generically define the type of [RecyclerView.ViewHolder] used
 * in [ListTypeAdapter]
 */
sealed class ListViewHolder(val view: View): RecyclerView.ViewHolder(view)

/**
 * A [ListViewHolder] that handles binding a [FirestoreUserSource] or [FirestoreGlobalSource] item
 */
class ListWordSourceViewHolder(
        view: View,
        private val listener: ListWordSourceListener
): ListViewHolder(view) {

    interface ListWordSourceListener {
        fun onWordClicked(word: String)
    }

    fun bind(source: WordSource) {
        //Set word
        when (source) {
            is FirestoreUserSource -> bindFirestoreUserSource(source)
            is FirestoreGlobalSource -> bindFirestoreGlobalSource(source)
        }
    }

    private fun bindFirestoreUserSource(source: FirestoreUserSource) {
        bindSource(
                source.userWord.word,
                source.userWord.partOfSpeechPreview,
                source.userWord.defPreview,
                source.userWord.synonymPreview

        )
    }

    private fun bindFirestoreGlobalSource(source: FirestoreGlobalSource) {
        bindSource(
                source.globalWord.word,
                source.globalWord.partOfSpeechPreview,
                source.globalWord.defPreview,
                source.globalWord.synonymPreview
        )
    }

    private fun bindSource(
            word: String,
            partOfSpeechPreview: MutableMap<String, String>,
            defPreview: MutableMap<String, String>,
            synonymPreview: MutableMap<String, String>
    ) {

        view.word.text = word

        //Set part of speech
        view.partOfSpeech.text = partOfSpeechPreview.keys.first()

        //Set definition
        defPreview.map { it.key }.firstOrNull()?.let {
            view.definition.text = it
        }

        //Set synonym chips
        view.chipGroup.removeAllViews()
        synonymPreview.forEach {
            val synonym = Synonym(it.key, OffsetDateTime.now(), OffsetDateTime.now())
            view.chipGroup.addView(
                    synonym.toChip(view.context, view.chipGroup) {
                        listener.onWordClicked(it.synonym)
                    }
            )
        }

        view.itemContainer.setOnClickListener {
            listener.onWordClicked(word)
        }
    }

}

/**
 * A [ListViewHolder] that handles binding a [HeaderBanner] with [HeaderBannerBinder]
 */
class ListHeaderViewHolder(
        view: View,
        private val listener: HeaderBannerListener
): ListViewHolder(view), HeaderBannerBinder {

    fun bind(banner: HeaderBanner?) {
        bindHeaderBanner(view.banner, banner, listener)
    }
}
