package space.narrate.waylan.merriamwebster.ui

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import space.narrate.waylan.core.util.fromHtml
import space.narrate.waylan.merriamwebster.R
import space.narrate.waylan.merriamwebster.util.toRelatedChip
import space.narrate.waylan.android.R as appR
import space.narrate.waylan.core.R as coreR


/**
 * A sealed class which holds object that know how to bind a [MerriamWebsterList] to the view
 * which holds its data.
 */
sealed class MerriamWebsterItemBinder<T : MerriamWebsterItemModel> {

    abstract val layout: Int

    abstract fun bind(view: View, item: T, listener: MerriamWebsterItemAdapter.Listener)

    object PartOfSpeechBinder :
        MerriamWebsterItemBinder<MerriamWebsterItemModel.PartOfSpeechModel>() {

        override val layout: Int = appR.layout.details_part_of_speech_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.PartOfSpeechModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            (view as? AppCompatTextView)?.text = item.partOfSpeech
        }
    }

    object DefinitionBinder : MerriamWebsterItemBinder<MerriamWebsterItemModel.DefinitionModel>() {

        override val layout: Int = appR.layout.details_definition_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.DefinitionModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            (view as? AppCompatTextView)?.text = item.def.fromHtml
        }
    }

    object RelatedBinder : MerriamWebsterItemBinder<MerriamWebsterItemModel.RelatedModel>() {

        override val layout: Int = R.layout.merriam_webster_related_layout

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

        override val layout: Int = coreR.layout.add_on_permission_pane_layout

        override fun bind(
            view: View,
            item: MerriamWebsterItemModel.PermissionPaneModel,
            listener: MerriamWebsterItemAdapter.Listener
        ) {
            view.findViewById<MaterialButton>(appR.id.details_button).setOnClickListener {
                listener.onDetailsButtonClicked()
            }
            view.findViewById<MaterialButton>(appR.id.dismiss_button).setOnClickListener {
                listener.onDismissButtonClicked()
            }
        }
    }
}

