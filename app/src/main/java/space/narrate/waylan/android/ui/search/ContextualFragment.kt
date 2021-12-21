package space.narrate.waylan.android.ui.search

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.shape.MaterialShapeDrawable
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentContextualBinding
import space.narrate.waylan.android.ui.MainActivity
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.util.collapse
import space.narrate.waylan.android.util.expand
import space.narrate.waylan.android.util.hide
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.MathUtils
import space.narrate.waylan.core.util.themeColor

/**
 * A second bottom sheet that lives behind the SearchFragment sheet. This is used to give
 * secondary content about what is in the main fragment container. This Fragment presents
 * things like a filter for filtering a list.
 */
class ContextualFragment : Fragment() {

    private lateinit var binding: FragmentContextualBinding

    private val navigator: Navigator by inject()

    // MainViewModel owned by MainActivity and used to share data between MainActivity
    // and its child Fragments
    private val sharedViewModel: MainViewModel by sharedViewModel()

    private val viewModel: ContextualViewModel by viewModel()

    // The BottomSheetBehavior of this view.
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(requireView())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContextualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val materialShapeDrawable = MaterialShapeDrawable(
            requireContext(),
            null,
            DEF_STYLE_ATTR,
            DEF_STYLE_RES
        ).apply {
            initializeElevationOverlay(requireContext())
            elevation = binding.contextualFrame.elevation
            fillColor = ColorStateList.valueOf(
                requireContext().themeColor(R.attr.colorSurface)
            )
            // Add a stroke to emphasize the shadow on the top of this bottom sheet.
            // The stroke is very light as the sheet moves towards the bottom of the screen
            // due to how Android's light source, used for shadow calculation, works.
            strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.shadow_emphasis_color)
            )
            strokeWidth = 2F
        }
        ViewCompat.setBackground(binding.contextualFrame, materialShapeDrawable)

        binding.closeImageView.setOnClickListener {
            sharedViewModel.onClearListFilter()
        }

        sharedViewModel.shouldOpenContextualSheet.observe(viewLifecycleOwner) { event ->
            event.withUnhandledContent { expand() }
        }
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

    private fun setUpSheet() {

        bottomSheetBehavior.isFitToContents = true

        // Animate the switching of containers between the collapsed (peeked) state and the
        // expanded state. Peeking should just show the top collapsedContainer and the expanded
        // state should just show the expandedContainer
        (requireActivity() as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val collapsedContainerAlpha = MathUtils.normalize(
                offset,
                0.0F,
                0.5F,
                1.0F,
                0.0F
            )
            val expandedContainerAlpha = MathUtils.normalize(
                offset,
                0.5F,
                1.0F,
                0.0F,
                1.0F
            )
            binding.collapsedContainer.alpha = collapsedContainerAlpha
            binding.expandedContainer.alpha = expandedContainerAlpha
        }

        (requireActivity() as MainActivity).contextualSheetCallback
            .addOnStateChangedAction { _, state ->
                when (state) {
                    BottomSheetBehavior.STATE_HIDDEN -> sharedViewModel.onContextualSheetHidden()
                }
            }

        // Configure UI based on current destination
        navigator.currentDestination.observe(viewLifecycleOwner) { dest ->
            when (dest) {
                Destination.TRENDING -> setExpandedContainer("Filter trending")
                else -> { /* Ignore or add other filterable lists in the future */ }
            }
        }

        // Configure bottom sheet state and UI based on current filter.
        viewModel.contextualFilterModel.observe(viewLifecycleOwner) { model ->
            setCollapsedChips(model.filter)
            peekOrHide(
                model.isFilterable && model.filter.isNotEmpty(),
                model.filter.isNotEmpty()
            )
        }

    }


    private fun setExpandedContainer(title: String) {
        binding.run {
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
    }

    private fun setCollapsedChips(list: List<Period>) {
        binding.run {
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
        private const val DEF_STYLE_ATTR = R.attr.styleBottomSheetStandard
        private const val DEF_STYLE_RES = R.style.Widget_Waylan_BottomSheet_Standard
    }
}