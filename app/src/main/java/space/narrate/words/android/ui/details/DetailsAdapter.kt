package space.narrate.words.android.ui.details

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import space.narrate.words.android.data.repository.*
import space.narrate.words.android.data.repository.WordSource
import kotlin.reflect.KClass

/**
 * An adapter which is able to receive and collect [WordSource]s (diffing for most up to date
 * values) and display a mixed list of any [DetailsComponent]s.
 *
 * Use this Adapter by calling [DetailsAdapter.submitWordSource] to add or update any [WordSource]
 * to the adapter. If the [WordSource] has not previously been submitted, it will be added and
 * held in [WordSourceList] which will map the source to it's corresponding [DetailsComponent] which
 * will be added to the adapter (ex. submitting a [MerriamWebsterSource] will add a
 * [DetailsComponent.MerriamWebsterComponent] to the list). If the [WordSource] <i>is</i> already
 * in the adapter, the newly submitted [WordSource] will be checked against the existing value
 * in [WordSourceList], a new list of [DetailsComponent]s will be submitted to the adapter the old
 * list will be diffed against the new list to determine what should be added, updated or removed.
 *
 * [WordSourceList] is the class which handles collecting submitted [WordSource]s and mapping
 * sources to a list of [DetailsComponent]s. Once a List of [DetailsComponent]s is submitted to the
 * underlying [ListAdapter], the list adapter's [DiffUtil.ItemCallback] handles when to add, update
 * or remove [DetailsComponent]s. Diffing is handled by [DetailsComponent]s, each
 * implementing the [Diffable] interface.
 */
class DetailsAdapter(private val listener: Listener) : ListAdapter<DetailsComponent,
        DetailsComponentViewHolder>(diffCallback),
        DetailsComponentListener {

    interface Listener {
        fun onRelatedWordClicked(relatedWord: String)
        fun onSuggestionWordClicked(suggestionWord: String)
        fun onSynonymChipClicked(synonym: String)
        fun onAudioClipError(message: String)
        fun onPlayAudioClicked(url: String?)
        fun onStopAudioClicked()
        fun onMerriamWebsterDetailsClicked()
        fun onMerriamWebsterDismissClicked()
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<DetailsComponent>() {
            override fun areItemsTheSame(
                    oldItem: DetailsComponent,
                    newItem: DetailsComponent
            ) : Boolean {
                return oldItem.isSameAs(newItem)
            }
            override fun areContentsTheSame(
                    oldItem: DetailsComponent,
                    newItem: DetailsComponent
            ): Boolean {
                return oldItem.isContentSameAs(newItem)
            }

            override fun getChangePayload(
                    oldItem: DetailsComponent,
                    newItem: DetailsComponent
            ): Any? {
                return oldItem.getChangePayload(newItem)
            }
        }
    }

    private val sourceHolder = WordSourceList()

    /**
     * Add individual [WordSource]s to the adapter. Each source will be mapped to it's
     * corresponding [DetailsComponent] and and an internal list will be generated with it included
     * to be submitted to the adapter.
     */
    fun submitWordSource(source: WordSource) {
        if (sourceHolder.add(source)) {
            submitList(sourceHolder.getComponentsList())
        }
    }

    fun removeWordSource(type: KClass<out WordSource>) {
        if (sourceHolder.remove(type)) {
            submitList(sourceHolder.getComponentsList())
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsComponentViewHolder {
        return DetailsComponentViewHolder.createViewHolder(parent, viewType, this)
    }

    override fun onBindViewHolder(holder: DetailsComponentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onSynonymChipClicked(synonym: String) {
        listener.onSynonymChipClicked(synonym)
    }

    override fun onRelatedWordClicked(relatedWord: String) {
        listener.onRelatedWordClicked(relatedWord)
    }

    override fun onSuggestionWordClicked(suggestionWord: String) {
        listener.onSuggestionWordClicked(suggestionWord)
    }

    override fun onAudioPlayClicked(url: String?) {
        listener.onPlayAudioClicked(url)
    }

    override fun onAudioStopClicked() {
        listener.onStopAudioClicked()
    }

    override fun onAudioClipError(message: String) {
        listener.onAudioClipError(message)
    }

    override fun onMerriamWebsterDetailsClicked() {
        listener.onMerriamWebsterDetailsClicked()
    }

    override fun onMerriamWebsterDismissClicked() {
        listener.onMerriamWebsterDismissClicked()
    }
}