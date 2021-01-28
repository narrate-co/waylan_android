package space.narrate.waylan.merriamwebster_thesaurus.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.isValid
import space.narrate.waylan.core.merriamwebster_thesaurus.MerriamWebsterThesaurusCardListener
import space.narrate.waylan.core.ui.widget.TextLabelChip
import space.narrate.waylan.core.ui.widget.configureWithUserAddOn
import space.narrate.waylan.core.util.getFloat
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.toChip
import space.narrate.waylan.core.util.visible
import space.narrate.waylan.merriamwebster_thesaurus.R
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusEntry
import space.narrate.waylan.merriamwebster_thesaurus.databinding.MwThesaurusCardChipGroupLayoutBinding
import space.narrate.waylan.core.R as coreR

/**
 * A [MaterialCardView] that displays groups of thesaurus words for a given thesaurus entry.
 */
class MerriamWebsterThesaurusCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val textLabel: TextLabelChip
    private val listContainer: LinearLayout
    private val expandCollapseButton: Button

    private var listener: MerriamWebsterThesaurusCardListener? = null

    // Whether or not all available chips are being shown.
    private var isCollapsed = true
    // The currently displayed list of entries.
    private var source: List<ThesaurusEntry> = emptyList()
    // The currently used userAddOn state
    private var userAddOn: UserAddOn? = null

    init {
        val view = View.inflate(context, R.layout.mw_thesaurus_card_layout, this)
        textLabel = view.findViewById(R.id.text_label)
        listContainer = view.findViewById(R.id.definitions_list_container)
        expandCollapseButton = view.findViewById(R.id.expand_collapse_button)
        background.alpha = (context.getFloat(coreR.dimen.translucence_01) * 255F).toInt()
        elevation = 0F

        // Configure views
        expandCollapseButton.setOnClickListener { expandCollapseGroups() }
        textLabel.setOnClickListener {
            listener?.onAddOnDetailsClicked(AddOn.MERRIAM_WEBSTER_THESAURUS)
        }
    }

    fun setSource(entries: List<ThesaurusEntry>, userAddOn: UserAddOn?) {
        // Reset to collapsed state if the current word is changing
        if (entries.firstOrNull()?.word != source.firstOrNull()?.word) {
            isCollapsed = true
            updateExpandCollapseButton()
        }

        this.source = entries
        this.userAddOn = userAddOn

        textLabel.configureWithUserAddOn(userAddOn)
        replaceAllChipGroups(entries, isCollapsed, userAddOn)
    }

    fun setListener(listener: MerriamWebsterThesaurusCardListener) {
        this.listener = listener
    }

    /**
     * Remove all groups from the list container and replace them with [entries]. [collapsed] will
     * cap the number of chips added to each group at the maximum defined in this class' companion
     * object.
     */
    private fun replaceAllChipGroups(
        entries: List<ThesaurusEntry>,
        collapsed: Boolean,
        userAddOn: UserAddOn?
    ) {
        listContainer.removeAllViews()

        // Exit early if user is not valid, showing the permission pane.
        if (userAddOn?.isValid == false) {
            addPermissionPane()
            return
        }

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

        // Hide the expand/collapse button if there is not content from MW Thesaurus.
        expandCollapseButton.visibility =
            if (listContainer.childCount == 0) View.GONE else View.VISIBLE
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

        expandCollapseButton.visible()
        listContainer.addView(binding.root)
    }

    /**
     * Inflate and add a layout with showing a user's lack of access to this add-on and expose
     * options to dismiss or find out more details.
     */
    private fun addPermissionPane() {
        val view = LayoutInflater.from(context).inflate(
            coreR.layout.add_on_permission_pane_layout,
            listContainer,
            false
        )
        val detailsButton = view.findViewById<Button>(coreR.id.details_button)
        val dismissButton = view.findViewById<Button>(coreR.id.dismiss_button)

        detailsButton.setOnClickListener {
            listener?.onAddOnDetailsClicked(AddOn.MERRIAM_WEBSTER_THESAURUS)
        }

        dismissButton.setOnClickListener {
            listener?.onAddOnDismissClicked(AddOn.MERRIAM_WEBSTER_THESAURUS)
        }

        expandCollapseButton.gone()
        listContainer.addView(view)
    }

    /**
     * Toggle the expanded state of all chip groups.
     */
    private fun expandCollapseGroups() {
        isCollapsed = !isCollapsed
        updateExpandCollapseButton()
        replaceAllChipGroups(source, isCollapsed, userAddOn)
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