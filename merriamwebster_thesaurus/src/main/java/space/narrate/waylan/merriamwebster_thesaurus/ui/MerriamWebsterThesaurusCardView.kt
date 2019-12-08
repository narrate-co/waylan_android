package space.narrate.waylan.merriamwebster_thesaurus.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.merriamwebster_thesaurus.MerriamWebsterThesaurusCardListener
import space.narrate.waylan.core.util.toChip
import space.narrate.waylan.merriamwebster_thesaurus.R
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.databinding.MwThesaurusCardChipGroupLayoutBinding

/**
 * A [MaterialCardView] that displays groups of thesaurus words for a given thesaurus entry.
 */
class MerriamWebsterThesaurusCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val listContainer: LinearLayout
    private val expandCollapseButton: Button

    private var listener: MerriamWebsterThesaurusCardListener? = null

    // Whether or not all available chips are being shown.
    private var isCollapsed = true
    // The currently displayed list of entries.
    private var source: List<ThesaurusEntry> = emptyList()

    init {
        val view = View.inflate(context, R.layout.mw_thesaurus_card_layout, this)
        listContainer = view.findViewById(R.id.list_container)
        expandCollapseButton = view.findViewById(R.id.expand_collapse_button)

        // Configure views
        expandCollapseButton.setOnClickListener { expandCollapseGroups() }
    }

    fun setSource(entries: List<ThesaurusEntry>, user: User?) {
        // Reset to collapsed state if the current word is changing
        if (entries.firstOrNull()?.word != source.firstOrNull()?.word) {
            isCollapsed = true
            updateExpandCollapseButton()
        }

        source = entries

        replaceAllChipGroups(entries, isCollapsed)
    }

    fun setListener(listener: MerriamWebsterThesaurusCardListener) {
        this.listener = listener
    }

    /**
     * Remove all groups from the list container and replace them with [entries]. [collapsed] will
     * cap the number of chips added to each group at the maximum defined in this class' companion
     * object.
     */
    private fun replaceAllChipGroups(entries: List<ThesaurusEntry>, collapsed: Boolean) {
        listContainer.removeAllViews()

        addChipGroup(
            context.getString(R.string.mw_thesaurus_synonyms_title),
            entries.map { it.synonymWords }
                .flatten()
                .take(if (collapsed) COLLAPSED_MAX_SYNONYMS else Integer.MAX_VALUE)
        )
        addChipGroup(
            context.getString(R.string.mw_thesaurus_related_title),
            entries.map { it.relatedWords }
                .flatten()
                .take(if (collapsed) COLLAPSED_MAX_RELATED else Integer.MAX_VALUE)
        )
        addChipGroup(
            context.getString(R.string.mw_thesaurus_near_title),
            entries.map { it.nearWords }
                .flatten()
                .take(if (collapsed) COLLAPSED_MAX_NEAR else Integer.MAX_VALUE)
        )
        addChipGroup(
            context.getString(R.string.mw_thesaurus_antonyms_title),
            entries.map { it.antonymWords }
                .flatten()
                .take(if (collapsed) COLLAPSED_MAX_ANTONYMS else Integer.MAX_VALUE)
        )
    }

    /**
     * Inflate and add a layout containing a title and a chip group containing all strings in the
     * given [list] to [listContainer].
     */
    private fun addChipGroup(title: String, list: List<String>) {
        if (list.isEmpty()) return
        val binding = MwThesaurusCardChipGroupLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            false
        )
        binding.chipSubtitle.text = title
        list.forEach {word ->
            val chip = word.toChip(context, binding.chipGroup) {
                listener?.onMwThesaurusChipClicked(it)
            }
            binding.chipGroup.addView(chip)
        }
        listContainer.addView(binding.root)
    }

    /**
     * Toggle the expanded state of all chip groups.
     */
    private fun expandCollapseGroups() {
        isCollapsed = !isCollapsed
        updateExpandCollapseButton()
        replaceAllChipGroups(source, isCollapsed)
    }

    private fun updateExpandCollapseButton() {
        expandCollapseButton.text = context.getString(
            if (isCollapsed) {
                R.string.mw_thesaurus_expand_chip_groups_button_title
            } else {
                R.string.mw_thesaurus_collapse_chip_groups_button_title
            }
        )
    }

    companion object {
        private const val COLLAPSED_MAX_SYNONYMS = 6
        private const val COLLAPSED_MAX_RELATED = 3
        private const val COLLAPSED_MAX_NEAR = 3
        private const val COLLAPSED_MAX_ANTONYMS = 3
    }

}