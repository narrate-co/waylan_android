package space.narrate.waylan.merriamwebster.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import space.narrate.waylan.core.data.firestore.users.AddOnState
import space.narrate.waylan.core.data.firestore.users.AddOnState.*
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.remainingDays
import space.narrate.waylan.core.data.firestore.users.state
import space.narrate.waylan.core.merriamwebster.MerriamWebsterCardListener
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.merriamwebster.R
import space.narrate.waylan.android.R as appR
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
    private val textLabel: Chip
    private val mwAudioView: MerriamWebsterAudioView

    init {
        val view = View.inflate(context, R.layout.merriam_webster_card_layout, this)
        listContainer = view.findViewById(R.id.list_container)
        textLabel = view.findViewById(R.id.text_label)
        mwAudioView = view.findViewById(R.id.mw_audio_view)
        mwAudioView.listener = this

        adapter = MerriamWebsterItemAdapter(listContainer, this)

        textLabel.setOnClickListener {
            listener?.onMwPermissionPaneDetailsClicked()
        }

    }

    fun setSource(entries: List<MwWordAndDefinitionGroups>, userAddOn: UserAddOn?) {
        setTextLabel(userAddOn)
        mwAudioView.setSource(entries, userAddOn)
        adapter.submit(entries, userAddOn)
    }

    fun setListener(listener: MerriamWebsterCardListener) {
        this.listener = listener
    }

    private fun setTextLabel(userAddOn: UserAddOn?) {
        if (userAddOn == null) {
            textLabel.gone()
            return
        }

        val text = when (userAddOn.state) {
            FREE_TRIAL_VALID -> {
                resources.getString(
                    appR.string.mw_card_view_free_trial_days_remaining,
                    userAddOn.remainingDays.toString()
                )
            }
            FREE_TRIAL_EXPIRED -> {
                resources.getString(appR.string.mw_card_view_free_trial_expired)
            }
            PURCHASED_VALID ->
                resources.getString(appR.string.mw_card_view_plugin_expired)
            PURCHASED_EXPIRED ->
                resources.getString(
                    appR.string.mw_card_view_renew_days_remaining,
                    userAddOn.remainingDays.toString()
                )
            else -> ""
        }

        val visibility = when (userAddOn.state) {
            FREE_TRIAL_VALID,
            FREE_TRIAL_EXPIRED,
            PURCHASED_EXPIRED -> View.VISIBLE
            else -> View.GONE
        }

        textLabel.text = text
        textLabel.visibility = visibility
    }

    override fun onRelatedWordClicked(word: String) {
        listener?.onMwRelatedWordClicked(word)
    }

    override fun onDetailsButtonClicked() {
        listener?.onMwPermissionPaneDetailsClicked()
    }

    override fun onDismissButtonClicked() {
        listener?.onMwPermissionPaneDismissClicked()
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

