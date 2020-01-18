package space.narrate.waylan.android.ui.search

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.FragmentSearchBinding
import space.narrate.waylan.android.ui.MainActivity
import space.narrate.waylan.android.ui.MainViewModel
import space.narrate.waylan.android.util.KeyboardManager
import space.narrate.waylan.android.util.collapse
import space.narrate.waylan.android.util.expand
import space.narrate.waylan.android.util.hide
import space.narrate.waylan.core.data.prefs.RotationManager
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.MathUtils
import space.narrate.waylan.core.util.displayHeightPx
import space.narrate.waylan.core.util.fadeThroughTransition
import space.narrate.waylan.core.util.getColorFromAttr
import space.narrate.waylan.core.util.hideSoftKeyboard
import space.narrate.waylan.core.util.showSoftKeyboard
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A bottom sheet fragment that handles user search input, current word action items (share,
 * favorite), the search shelf (contextual suggestions which expand into the space above the
 * input bar) and displaying lists of recently searched words if the search field is empty
 * or search results when not.
 *
 * This Fragment is designed to be ergonomic and easy to use one handed. The most popular
 * user journey is to search and define a word. This journey should be as ergonomic, quick and
 * seamless as possible.
 */
class SearchFragment : Fragment(), SearchItemAdapter.SearchItemListener, TextWatcher {

    private lateinit var binding: FragmentSearchBinding

    private val navigator: Navigator by inject()

    // MainViewModel owned by MainActivity and used to share data between MainActivity
    // and its child Fragments
    private val sharedViewModel: MainViewModel by sharedViewModel()

    // SearchFragment's own ViewModel
    private val viewModel: SearchViewModel by viewModel()

    // The BottomSheetBehavior of this view.
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(requireView())
    }

    private val adapter by lazy { SearchItemAdapter(this) }

    private val rotationManager: RotationManager by inject()

    private var numberOfShelfActionsShowing: Int = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
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
            elevation = binding.collapsedContainer.elevation
            fillColor = ColorStateList.valueOf(
                requireContext().getColorFromAttr(R.attr.colorSurface)
            )
            // Add a stroke to emphasize the shadow on the top of this bottom sheet.
            // The stroke is very light as the sheet moves towards the bottom of the screen
            // due to how Android's light source, used for shadow calculation, works.
            strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.shadow_emphasis_color)
            )
            strokeWidth = 3F
        }
        ViewCompat.setBackground(binding.collapsedContainer, materialShapeDrawable)

        sharedViewModel.currentWord.observe(this) {
            viewModel.onCurrentWordChanged(it)
        }

        sharedViewModel.shouldOpenAndFocusSearch.observe(this) { event ->
            event.withUnhandledContent { focusAndOpenSearch() }
        }

        viewModel.shouldShowDetails.observe(this) { event ->
            event.withUnhandledContent {
                sharedViewModel.onChangeCurrentWord(it)
                val navController = (requireActivity() as MainActivity).findNavController()
                if (navController.currentDestination?.id != R.id.detailsFragment) {
                    navController.navigate(R.id.action_global_detailsFragment)
                }
            }
        }

        navigator.currentDestination.observe(this) {
            when (it) {
                Destination.SETTINGS,
                Destination.ADD_ONS,
                Destination.ABOUT,
                Destination.THIRD_PARTY,
                Destination.DEV_SETTINGS -> {
                    bottomSheetBehavior.isHideable = true
                    bottomSheetBehavior.hide(requireActivity())
                }
                else -> {
                    bottomSheetBehavior.isHideable = false
                }
            }
        }

        viewModel.shouldCloseKeyboard.observe(this) {
            requireActivity().hideSoftKeyboard()
        }

        setUpSheet()

        setUpSearchBar()

        setUpRecyclerView()

        setUpSmartShelf()

        setUpShelfActions()
    }

    fun close(): Boolean {
        return bottomSheetBehavior.collapse(requireActivity())
    }

    fun handleOnBackPressed(): Boolean {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED ||
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            return close()
        }
        return false
    }

    private fun setUpSheet() {

        // Set max expanded height to 60% of screen height, the max height it can be expected that
        // a person can reach with their thumb
        val maxReachableExpandedHeight = (requireContext().displayHeightPx * .60F).roundToInt()
        requireView().layoutParams?.height = maxReachableExpandedHeight

        // Observe the keyboard height so we can move the entire bottom sheet up and keep it in
        // view when the keyboard is on screen.
        KeyboardManager(requireActivity(), binding.collapsedContainer)
            .getKeyboardHeightData()
            .observe(this) {
                viewModel.onSoftInputChanged(it)
            }

        viewModel.keyboardHeight.observe(this) {
            requireView().translationY = -it
        }

        (requireActivity() as MainActivity).run {
            searchSheetCallback.currentSlideLive.observe(this@SearchFragment) {
                viewModel.onSearchSheetOffsetChanged(it)
            }

            searchSheetCallback.currentStateLive.observe(this@SearchFragment) {
                viewModel.onSearchSheetStateChanged(it)
            }

            contextualSheetCallback.currentSlideLive.observe(this@SearchFragment) {
                viewModel.onContextualSheetOffsetChanged(it)
            }

            contextualSheetCallback.currentStateLive.observe(this@SearchFragment) {
                viewModel.onContextualSheetStateChanged(it)
            }
        }

        viewModel.shouldCloseSheet.observe(this) {
            bottomSheetBehavior.collapse(requireActivity())
        }

        // Pin the search bar to the bottom of the screen by observing the sheets slide.
        (requireActivity() as MainActivity).searchSheetCallback.addOnSlideAction { view, offset ->
            val delta = (view.height - bottomSheetBehavior.peekHeight) * offset
            binding.run {
                // Adjust the margins of the search container to allow the search results
                // recyclerview to recalculate its available space and increase its height.
                val params = searchContainer.layoutParams as
                    ConstraintLayout.LayoutParams
                val originalTopMargin = searchContainer.resources.getDimensionPixelSize(R.dimen.search_input_area_margin_top)
                params.updateMargins(top = delta.toInt() + originalTopMargin)
                searchContainer.layoutParams = params
            }
        }

        binding.recyclerView.alpha = 0F
        (requireActivity() as MainActivity).searchSheetCallback.addOnSlideAction { _, offset ->
            binding.recyclerView.alpha = MathUtils.normalize(
                offset,
                0.2F,
                1.0F,
                0.0F,
                1.0F
            )
        }

        // Make sure we're hiding the recycler view, even when the state change event is
        // coming from something other than a drag.
        (requireActivity() as MainActivity).searchSheetCallback
            .addOnStateChangedAction { _, newState ->
                if (newState == BottomSheetBehavior.STATE_HIDDEN ||
                    newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.recyclerView.alpha = 0F
                }
            }
    }

    // Set up textRes watchers and on focus changed listeners to help control the
    // hiding/showing of both the search sheet and the IME
    private fun setUpSearchBar() {
        binding.searchEditText.addTextChangedListener(this)

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.expand()
            } else {
                bottomSheetBehavior.collapse(activity)
            }
        }

        binding.searchEditText.setOnClickListener {
            if (binding.searchEditText.hasFocus()) {
                bottomSheetBehavior.expand()
            }
        }
    }

    // Set up the recycler view which holds recently viewed words when the search input field
    // is empty and search results when not.
    private fun setUpRecyclerView() {
        //set up recycler view
        binding.recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            true
        )
        binding.recyclerView.adapter = adapter

        // hide IME if user is scrolling search results
        // This avoids the need to expand the search sheet to the full height of the display
        // and moving results out of "thumb reach"
        binding.recyclerView.setOnTouchListener { _, _ ->
            requireActivity().hideSoftKeyboard()
            false
        }

        viewModel.searchResults.observe(this) {
            adapter.submitList(it)
            binding.recyclerView.scrollToPosition(0)
        }
    }

    private fun setUpSmartShelf() {

        viewModel.shouldShowOrientationPrompt.observe(this) { event ->
            // TODO: Show a UI prompt for smart actions
        }

        // observe for orientation/rotation changes in the viewModel
        rotationManager.observe(this.javaClass.simpleName, this, viewModel)

        // observe for all orientation/rotation patterns in the viewModel
        rotationManager.observeForPattern(
                this.javaClass.simpleName,
                this,
                RotationManager.PATTERNS_ALL,
                viewModel)
    }

    // Shelf actions are actions which live to the right of the search input field. They
    // animate in, compressing the search input field, when the DetailsFragment is the current
    // Fragment and animate out when the search sheet is expanded or DetailsFragment is not the
    // current Fragment
    private fun setUpShelfActions() {
        // Hide actions when DetailsFragment is not the current Fragment, otherwise show
        viewModel.searchShelfModel.observe(this) { model ->
            // wait for the next layout step to grantee the actions.width is correctly captured
            if (binding.actionsContainer.isLaidOut) {
                when (model) {
                    is SearchShelfActionsModel.DetailsShelfActions -> {
                        runShelfActionsAnimation(model.numberOfActionsToShow)
                        setShelfActionsForDetails(model)
                    }
                    is SearchShelfActionsModel.ListShelfActions -> {
                        runShelfActionsAnimation(model.numberOfActionsToShow)
                        setShelfActionsForList(model)
                    }
                    is SearchShelfActionsModel.SheetKeyboardControllerActions -> {
                        setShelfActionForSheetKeyboardAction(model)
                    }
                    is SearchShelfActionsModel.None -> {
                        runShelfActionsAnimation(model.numberOfActionsToShow)
                    }
                }
            }
        }

        // Hide actions if sheet is expanded
        (activity as MainActivity).searchSheetCallback.addOnSlideAction { _, offset ->
            setSheetSlideOffsetForActions(
                    offset,
                    (requireActivity() as MainActivity).contextualSheetCallback.currentSlide
            )
        }
    }

    private fun setSheetSlideOffsetForActions(searchOffset: Float, contextualOffset: Float) {
        val collapsedMargin =  getSearchAreaMarginRightForNumberOfShowingActions(
            numberOfShelfActionsShowing
        )
        val expandedMargin = getSearchAreaMarginRightForNumberOfShowingActions(1)

        val params = binding.searchContainer.layoutParams as ConstraintLayout.LayoutParams

        params.rightMargin = MathUtils.normalize(
            max(searchOffset, contextualOffset),
            0F,
            1F,
            collapsedMargin.toFloat(),
            expandedMargin.toFloat()
        ).toInt()
        binding.searchContainer.layoutParams = params
    }

    private fun getSearchAreaMarginRightForNumberOfShowingActions(number: Int): Int {
        // Margin between the right edge of the search area and the right edge of the screen
        // when no actions are showing.
        val marginFromEdgeOfScreen = resources.getDimensionPixelSize(R.dimen.keyline_2)

        // Total horizontal margins for each action item.
        val params = binding.actionOne.layoutParams as LinearLayout.LayoutParams
        val actionMarginStartEnd = params.leftMargin + params.rightMargin

        // Width of an action including its margins. This assumes all actions are the same width.
        val actionWidth = binding.actionOne.width + actionMarginStartEnd

        return marginFromEdgeOfScreen + (actionWidth * number)
    }

    /**
     * A method which can be called from this Fragment's parent Activity. If the parent
     * Activity or other visible Fragments would like to trigger search sheet expanding and
     * IME opening to initiate a search, use this method
     */
    private fun focusAndOpenSearch() {
        bottomSheetBehavior.expand()
        binding.searchEditText.requestFocus()
        requireActivity().showSoftKeyboard(binding.searchEditText)
    }

    /**
     * Animate in or out the shelf actions (the actions which live to the right of the search
     * input field) by animating the right margin of the search input layout.
     */
    private fun runShelfActionsAnimation(numberOfActions: Int) {
        if (!isAdded || numberOfShelfActionsShowing == numberOfActions) return

        numberOfShelfActionsShowing = numberOfActions
        val newMargin = getSearchAreaMarginRightForNumberOfShowingActions(numberOfActions)
        val currentMargin =
            (binding.searchContainer.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if (currentMargin == newMargin) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = binding.searchContainer.layoutParams as ConstraintLayout.LayoutParams
                params.rightMargin = MathUtils.normalize(
                    interpolatedTime,
                    0F,
                    1F,
                    currentMargin.toFloat(),
                    newMargin.toFloat()
                ).toInt()
                binding.searchContainer.layoutParams = params
            }
        }
        animation.duration = 200
        animation.interpolator = FastOutSlowInInterpolator()
        binding.searchContainer.startAnimation(animation)
    }

    /**
     * Set shelf actions according to [userWord]
     */
    private fun setShelfActionsForDetails(model: SearchShelfActionsModel.DetailsShelfActions) {

        binding.run {
            actionOneImageView.setOnClickListener {
                viewModel.onFavoriteUnfavoriteShelfActionClicked(model)
            }
            actionOneImageView.fadeThroughTransition {
                setImageResource(model.actionOne.icon)
                contentDescription = getString(model.actionOne.contentDescription)
            }
            actionOne.background = null

            // TODO add share button setup
        }
    }

    private fun setShelfActionsForList(model: SearchShelfActionsModel.ListShelfActions) {
        binding.run {
            actionOneImageView.setOnClickListener {
                sharedViewModel.onShouldOpenContextualFragment()
            }
            actionOneImageView.fadeThroughTransition {
                setImageResource(model.actionOne.icon)
                contentDescription = getString(model.actionOne.contentDescription)
            }
            actionOne.background = null
        }
    }

    private fun setShelfActionForSheetKeyboardAction(
        model: SearchShelfActionsModel.SheetKeyboardControllerActions
    ) {
        binding.run {
            actionOneImageView.setOnClickListener {
                viewModel.onSheetKeyboardControllerShelfActionClicked(model)
            }
            actionOneImageView.fadeThroughTransition {
                setImageResource(model.actionOne.icon)
                contentDescription = getString(model.actionOne.contentDescription)
            }
            actionOne.setBackgroundResource(R.drawable.search_input_area)
        }
    }

    override fun onWordClicked(searchItem: SearchItemModel) {
        close()
        viewModel.onWordClicked(searchItem)
    }

    override fun afterTextChanged(s: Editable?) { }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.onSearchInputTextChanged(s)
    }

    override fun onBannerClicked() {
        // Do nothing. Search banner should not have any buttons
    }

    override fun onBannerLabelClicked() {
        // Do nothing. Search banner should not have a label.
    }

    override fun onBannerTopButtonClicked() {
        // Do nothing. Search banner should not have any buttons
    }

    override fun onBannerBottomButtonClicked() {
        // Do nothing. Search banner should not have any buttons
    }

    companion object {
        private const val DEF_STYLE_ATTR = R.attr.styleBottomSheetStandard
        private const val DEF_STYLE_RES = R.style.Widget_Waylan_BottomSheet_Standard
    }
}

