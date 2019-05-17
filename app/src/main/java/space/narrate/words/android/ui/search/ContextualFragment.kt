package space.narrate.words.android.ui.search

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.MaterialShapeDrawable
import space.narrate.words.android.MainActivity
import space.narrate.words.android.MainViewModel
import space.narrate.words.android.Navigator
import space.narrate.words.android.R
import space.narrate.words.android.ui.common.BaseUserFragment
import space.narrate.words.android.util.*

/**
 * A second bottom sheet that lives behind the SearchFragment sheet. This is used to give
 * secondary content about what is in the main fragment container. This Fragment presents
 * things like a filter for filtering a list.
 */
class ContextualFragment : BaseUserFragment() {

    private lateinit var contextualFrame: FrameLayout
    private lateinit var closeImageView: AppCompatImageView
    private lateinit var collapsedContainer: ConstraintLayout
    private lateinit var expandedContainer: ConstraintLayout
    private lateinit var titleTextView: AppCompatTextView
    private lateinit var collapsedChipGroup: ChipGroup
    private lateinit var expandedChipGroup: ChipGroup


    // MainViewModel owned by MainActivity and used to share data between MainActivity
    // and its child Fragments
    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    // The BottomSheetBehavior of this view.
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contextual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contextualFrame = view.findViewById(R.id.contextual_frame)
        closeImageView = view.findViewById(R.id.close_image_view)
        collapsedContainer = view.findViewById(R.id.collapsed_container)
        expandedContainer = view.findViewById(R.id.expanded_container)
        titleTextView = view.findViewById(R.id.title_text_view)
        collapsedChipGroup = view.findViewById(R.id.collapsed_chip_group)
        expandedChipGroup = view.findViewById(R.id.expanded_chip_group)

        val materialShapeDrawable = MaterialShapeDrawable(
            requireContext(),
            null,
            R.attr.styleBottomSheetStandard,
            R.style.Widget_Words_BottomSheet_Standard
        ).apply {
            initializeElevationOverlay(requireContext())
            elevation = contextualFrame.elevation
            fillColor = ColorStateList.valueOf(requireContext().getColorFromAttr(R.attr.colorSurface))
        }
        ViewCompat.setBackground(contextualFrame, materialShapeDrawable)

        closeImageView.setOnClickListener {
            sharedViewModel.onClearListFilter()
        }

        sharedViewModel.shouldOpenContextualSheet.observe(this, Observer { event ->
            event.getUnhandledContent()?.let { expand() }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Set default values before observables emit values.
        setUpSheet()

        setExpandedContainer("Filter")
        hide()
    }

    fun handleOnBackPressed(): Boolean {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            return close()
        }

        return false
    }

    override fun handleApplyWindowInsets(insets: WindowInsetsCompat): WindowInsetsCompat {
        val searchPeekHeight = SearchFragment.getPeekHeight(requireContext(), insets)

        contextualFrame.updatePadding(
            left = insets.systemWindowInsetLeft,
            right = insets.systemWindowInsetRight,
            bottom = searchPeekHeight
        )

        bottomSheetBehavior.peekHeight = getPeekHeight(requireContext(), insets)

        return super.handleApplyWindowInsets(insets)
    }

    private fun setUpSheet() {

        bottomSheetBehavior.isFitToContents = true

        // Animate the switching of containers between the collapsed (peeked) state and the
        // expanded state. Peeking should just show the top collapsedContainer and the expanded
        // state should just show the expandedContainer
        (requireActivity() as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val collapsedContainerAlpha = MathUtils.normalize(offset, 0.0F, 0.5F, 1.0F, 0.0F)
            val expandedContainerAlpha = MathUtils.normalize(offset, 0.5F, 1.0F, 0.0F, 1.0F)
            collapsedContainer.alpha = collapsedContainerAlpha
            expandedContainer.alpha = expandedContainerAlpha
        }

        (requireActivity() as MainActivity).contextualSheetCallback.addOnStateChangedAction { _, state ->
            when (state) {
                BottomSheetBehavior.STATE_HIDDEN -> sharedViewModel.onContextualSheetHidden()
            }
        }

        // Configure UI based on current destination
        sharedViewModel.currentDestination.observe(this, Observer { dest ->
            when (dest) {
                Navigator.Destination.TRENDING -> setExpandedContainer("Filter trending")
            }
        })

        // Configure bottom sheet state and UI based on current filter.
        sharedViewModel.contextualFilterModel.observe(this, Observer { model ->
            setCollapsedChips(model.filter)
            peekOrHide(model.isFilterable && model.filter.isNotEmpty(), model.filter.isNotEmpty())
        })

    }


    private fun setExpandedContainer(title: String) {
        titleTextView.text = title
        expandedChipGroup.removeAllViews()
        Period.values().forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    expandedChipGroup,
                    false
            ) as Chip
            val label = getString(period.label)
            chip.text = label
            chip.setOnClickListener {
                sharedViewModel.onListFilterPeriodClicked(period)
            }
            expandedChipGroup.addView(chip)
        }
    }

    private fun setCollapsedChips(list: List<Period>) {
        collapsedChipGroup.removeAllViews()
        list.forEach { period ->
            val chip = LayoutInflater.from(context).inflate(
                    R.layout.contextual_chip_layout,
                    collapsedChipGroup,
                    false
            ) as Chip
            chip.text = getString(period.label)
            collapsedChipGroup.addView(chip)
        }
    }

    private fun peekOrHide(hasAppliedFilter: Boolean, isPeekable: Boolean) {
        bottomSheetBehavior.skipCollapsed = !isPeekable
        if (hasAppliedFilter) {
            peek()
        } else {
            bottomSheetBehavior.isHideable = !hasAppliedFilter
            hide()
        }
    }

    fun close(): Boolean {
        return if (bottomSheetBehavior.skipCollapsed && bottomSheetBehavior.isHideable) {
            hide()
        } else {
            peek()
        }
    }

    private fun expand(): Boolean {
        return bottomSheetBehavior.expand()
    }

    private fun peek(): Boolean {
        return bottomSheetBehavior.collapse()
    }

    private fun hide(): Boolean {
        return bottomSheetBehavior.hide()
    }

    companion object {
        const val TAG = "ContextualFragment"

        fun getPeekHeight(context: Context, insets: WindowInsetsCompat): Int {
            return SearchFragment.getPeekHeight(context, insets) +
                context.resources.getDimensionPixelSize(
                    R.dimen.contextual_collapsed_container_height
                )
        }
    }
}