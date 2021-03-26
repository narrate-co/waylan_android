package space.narrate.waylan.merriamwebster.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.merriamwebster.MerriamWebsterCardListener
import space.narrate.waylan.core.ui.widget.DictionaryEntryAudioHelper
import space.narrate.waylan.core.ui.widget.configureWithUserAddOn
import space.narrate.waylan.core.util.getFloat
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups
import space.narrate.waylan.merriamwebster.databinding.MerriamWebsterCardLayoutBinding
import space.narrate.waylan.core.R as coreR

/**
 * A composite view which is able to display all content retrieved from
 * Merriam-Webster for a given word.
 */
class MerriamWebsterCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr),
    MerriamWebsterItemAdapter.Listener {

    private var listener: MerriamWebsterCardListener? = null

    private var adapter: MerriamWebsterItemAdapter

    private val binding: MerriamWebsterCardLayoutBinding =
        MerriamWebsterCardLayoutBinding.inflate(LayoutInflater.from(context), this)

    private val audioHelper: DictionaryEntryAudioHelper = DictionaryEntryAudioHelper()

    init {
        background.alpha = (context.getFloat(coreR.dimen.translucence_01) * 255F).toInt()
        elevation = 0F

        adapter = MerriamWebsterItemAdapter(binding.definitionsListContainer, this)

        binding.textLabel.setOnClickListener {
            listener?.onAddOnDetailsClicked(AddOn.MERRIAM_WEBSTER)
        }
    }

    fun setSource(entries: List<MwWordAndDefinitionGroups>, userAddOn: UserAddOn?) {
        binding.textLabel.configureWithUserAddOn(userAddOn)
        adapter.submit(entries, userAddOn)
    }

    fun setAudio(
        urls: List<String>,
        userAddOn: UserAddOn?,
        listener: DictionaryEntryAudioHelper.Listener
    ) {
        audioHelper.setSources(binding.actionView, urls, userAddOn, listener)
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
}

