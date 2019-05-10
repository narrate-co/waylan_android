package space.narrate.words.android.ui.details

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import space.narrate.words.android.R
import space.narrate.words.android.util.fromHtml
import space.narrate.words.android.util.toRelatedChip

/**
 * A sealed class which holds object that know how to bind a [MerriamWebsterList] to the view
 * which holds its data.
 */
sealed class MerriamWebsterItemBinder<T : MerriamWebsterItemModel> {

    abstract val layout: Int

    abstract fun bind(view: View, item: T, listener: MerriamWebsterItemAdapter.Listener)

    object PartOfSpeechBinder :
        MerriamWebsterItemBinder<MerriamWebsterItemModel.PartOfSpeechModel>() {

        override val layout: Int = R.layout.details_part_of_speech_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.PartOfSpeechModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            (view as? AppCompatTextView)?.text = item.partOfSpeech
        }
    }

    object DefinitionBinder : MerriamWebsterItemBinder<MerriamWebsterItemModel.DefinitionModel>() {

        override val layout: Int = R.layout.details_definition_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.DefinitionModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            (view as? AppCompatTextView)?.text = item.def.fromHtml
        }
    }

    object RelatedBinder : MerriamWebsterItemBinder<MerriamWebsterItemModel.RelatedModel>() {

        override val layout: Int = R.layout.details_related_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.RelatedModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            val chipGroup = view.findViewById<ChipGroup>(R.id.related_chip_group)
            chipGroup.removeAllViews()
            item.words.forEach { word ->
                chipGroup.addView(word.toRelatedChip(view.context, chipGroup) {
                    listener.onRelatedWordClicked(it)
                })
            }
        }
    }

    object PermissionPaneBinder :
        MerriamWebsterItemBinder<MerriamWebsterItemModel.PermissionPaneModel>() {

        override val layout: Int = R.layout.details_mw_permission_pane_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.PermissionPaneModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            view.findViewById<MaterialButton>(R.id.details_button).setOnClickListener {
                listener.onDetailsButtonClicked()
            }
            view.findViewById<MaterialButton>(R.id.dismiss_button).setOnClickListener {
                listener.onDismissButtonClicked()
            }
        }
    }
}

