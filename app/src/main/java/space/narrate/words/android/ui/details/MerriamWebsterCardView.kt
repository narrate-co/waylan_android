package space.narrate.words.android.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.mw.MwWordAndDefinitionGroups
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState

class MerriamWebsterCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr),
    MerriamWebsterItemAdapter.Listener,
    MerriamWebsterAudioView.Listener {

    interface Listener {
        fun onMwRelatedWordClicked(word: String)
        fun onMwSuggestionWordClicked(word: String)
        fun onMwAudioPlayClicked(url: String?)
        fun onMwAudioStopClicked()
        fun onMwAudioClipError(messageRes: Int)
        fun onMwPermissionPaneDetailsClicked()
        fun onMwPermissionPaneDismissClicked()
    }

    private var listener: Listener? = null

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

    fun setSource(entries: List<MwWordAndDefinitionGroups>, user: User?) {
        setTextLabel(user)
        mwAudioView.setSource(entries, user)
        adapter.submit(entries, user)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun setTextLabel(user: User?) {
        val state = user?.merriamWebsterState
        when (state) {
            is PluginState.FreeTrial -> {
                if (state.isValid) {
                    textLabel.text = resources.getString(
                        R.string.mw_card_view_free_trial_days_remaining,
                        state.remainingDays.toString()
                    )
                } else {
                    textLabel.text = resources.getString(R.string.mw_card_view_free_trial_expired)
                }
                textLabel.visibility = View.VISIBLE
            }
            is PluginState.Purchased -> {
                if (!state.isValid) {
                    // show labelRes
                    textLabel.text = resources.getString(R.string.mw_card_view_plugin_expired)
                    textLabel.visibility = View.VISIBLE
                } else if (state.remainingDays <= 7L) {
                    // hide labelRes
                    textLabel.text = resources.getString(
                        R.string.mw_card_view_renew_days_remaining,
                        state.remainingDays.toString()
                    )
                    textLabel.visibility = View.VISIBLE
                } else {
                    textLabel.visibility = View.GONE
                }
            }
            else -> textLabel.visibility = View.GONE
        }
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

