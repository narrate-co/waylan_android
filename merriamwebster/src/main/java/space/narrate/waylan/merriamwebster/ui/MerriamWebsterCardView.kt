package space.narrate.waylan.merriamwebster.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.merriamwebster.MerriamWebsterCardListener
import space.narrate.waylan.core.ui.widget.TextLabelChip
import space.narrate.waylan.core.ui.widget.configureWithUserAddOn
import space.narrate.waylan.core.R as coreR
import space.narrate.waylan.core.util.getFloat
import space.narrate.waylan.merriamwebster.R
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups

/**
 * A composite view which is able to display all content retrieved from
 * Merriam-Webster for a given word.
 */
class MerriamWebsterCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr),
    MerriamWebsterItemAdapter.Listener,
    MerriamWebsterAudioView.Listener {

    private var listener: MerriamWebsterCardListener? = null

    private var adapter: MerriamWebsterItemAdapter

    private val listContainer: LinearLayout
    private val textLabel: TextLabelChip
    private val mwAudioView: MerriamWebsterAudioView

    init {
        val view = View.inflate(context, R.layout.merriam_webster_card_layout, this)
        listContainer = view.findViewById(R.id.definitions_list_container)
        textLabel = view.findViewById(R.id.text_label)
        mwAudioView = view.findViewById(R.id.mw_audio_view)
        mwAudioView.listener = this
        background.alpha = (context.getFloat(coreR.dimen.translucence_01) * 255F).toInt()
        elevation = 0F

        adapter = MerriamWebsterItemAdapter(listContainer, this)

        textLabel.setOnClickListener {
            listener?.onAddOnDetailsClicked(AddOn.MERRIAM_WEBSTER)
        }

    }

    fun setSource(entries: List<MwWordAndDefinitionGroups>, userAddOn: UserAddOn?) {
        textLabel.configureWithUserAddOn(userAddOn)
        mwAudioView.setSource(entries, userAddOn)
        adapter.submit(entries, userAddOn)
    }

    fun setListener(listener: MerriamWebsterCardListener) {
        this.listener = listener
    }

    override fun onRelatedWordClicked(word: String) {
        listener?.onMwRelatedWordClicked(word)
    }

    override fun onDetailsButtonClicked() {
        listener?.onAddOnDetailsClicked(AddOn.MERRIAM_WEBSTER)
    }

    override fun onDismissButtonClicked() {
        listener?.onAddOnDismissClicked(AddOn.MERRIAM_WEBSTER)
    }

    override fun onAudioPlayClicked(url: String) {
        listener?.onMwAudioPlayClicked(url)
    }

    override fun onAudioStopClicked() {
        listener?.onMwAudioStopClicked()
    }

    override fun onAudioError(messageRes: Int) {
        listener?.onMwAudioClipError(messageRes)
    }
}

