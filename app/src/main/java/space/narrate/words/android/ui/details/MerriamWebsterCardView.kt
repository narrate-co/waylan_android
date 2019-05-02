package space.narrate.words.android.ui.details

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import space.narrate.words.android.R
import space.narrate.words.android.data.disk.mw.PermissiveWordsDefinitions
import space.narrate.words.android.data.firestore.users.PluginState
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState

class MerriamWebsterCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr),
        MerriamWebsterListAdapter.Listener,
        MerriamWebsterAudioView.Listener {

    interface MerriamWebsterViewListener {
        fun onRelatedWordClicked(word: String)
        fun onSuggestionWordClicked(word: String)
        fun onAudioPlayClicked(url: String?)
        fun onAudioStopClicked()
        fun onAudioClipError(message: String)
        fun onPermissionPaneDetailsClicked()
        fun onPermissionPaneDismissClicked()
    }

    private var listener: MerriamWebsterViewListener? = null

    private var adapter: MerriamWebsterListAdapter

    private val listContainer: LinearLayout
    private val textLabel: Chip
    private val mwAudioView: MerriamWebsterAudioView

    init {
        val view = View.inflate(context, R.layout.merriam_webster_card_layout, this)
        listContainer = view.findViewById(R.id.list_container)
        textLabel = view.findViewById(R.id.text_label)
        mwAudioView = view.findViewById(R.id.mw_audio_view)
        mwAudioView.listener = this

        adapter = MerriamWebsterListAdapter(listContainer, this)

        textLabel.setOnClickListener {
            listener?.onPermissionPaneDetailsClicked()
        }

    }

    fun setSource(wordsAndDefinitions: PermissiveWordsDefinitions?) {
        setTextLabel(wordsAndDefinitions?.user)
        val entries = wordsAndDefinitions?.entries ?: emptyList()
        mwAudioView.setSource(entries, wordsAndDefinitions?.user)
        adapter.submit(entries, wordsAndDefinitions?.user)
    }

    fun addListener(listener: MerriamWebsterViewListener) {
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
                    // show label
                    textLabel.text = resources.getString(R.string.mw_card_view_plugin_expired)
                    textLabel.visibility = View.VISIBLE
                } else if (state.remainingDays <= 7L) {
                    // hide label
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
        listener?.onRelatedWordClicked(word)
    }

    override fun onDetailsButtonClicked() {
        listener?.onPermissionPaneDetailsClicked()
    }

    override fun onDismissButtonClicked() {
        listener?.onPermissionPaneDismissClicked()
    }

    override fun onAudioPlayClicked(url: String) {
        listener?.onAudioPlayClicked(url)
    }

    override fun onAudioStopClicked() {
        listener?.onAudioStopClicked()
    }

    override fun onAudioError(message: String) {
        listener?.onAudioClipError(message)
    }
}

