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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
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
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.firestore.users.UserWordType
import space.narrate.waylan.core.data.prefs.RotationManager
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.MathUtils
import space.narrate.waylan.core.util.displayHeightPx
import space.narrate.waylan.core.util.getColorFromAttr
import space.narrate.waylan.core.util.getDimensionPixelSizeFromAttr
import space.narrate.waylan.core.util.hideSoftKeyboard
import space.narrate.waylan.core.util.showSoftKeyboard
import space.narrate.waylan.core.util.swapImageResource
import kotlin.math.max

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
        val maxReachableExpandedHeight = Math.round(requireContext().displayHeightPx * .60F)
        val searchItemHeight = requireContext().getDimensionPixelSizeFromAttr(
            android.R.attr.listPreferredItemHeight
        )
        val minPeekHeight = resources.getDimensionPixelSize(R.dimen.search_min_peek_height)
        val minVisibleHeightAboveKeyboard = minPeekHeight + (1.5 * searchItemHeight)

        requireView().layoutParams?.height = maxReachableExpandedHeight

        // Observe the height of the keyboard. If it is taller than the search bar + 1.5 search
        // result list items (keep a few list items visible so the user knows there are immediate
        // results), reset the height of the search sheet.
        KeyboardManager(requireActivity(), binding.collapsedContainer)
                .getKeyboardHeightData()
                .observe(this) {
                    val minHeight = max(
                            maxReachableExpandedHeight,
                            (it.height + minVisibleHeightAboveKeyboard).toInt()
                    )
                    if (it.height != 0 && minHeight != requireView().layoutParams.height) {
                        requireView().layoutParams.height = minHeight
                    }
                }

        binding.recyclerView.alpha = 0F
        (requireActivity() as MainActivity).searchSheetCallback.addOnSlideAction { _, offset ->
            binding.recyclerView.alpha = MathUtils.normalize(offset, 0.2F, 1.0F, 0.0F, 1.0F)
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
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
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
        sharedViewModel.searchShelfModel.observe(this) { model ->
            // wait for the next layout step to grantee the actions.width is correctly captured
            view?.post {
                when (model) {
                    is SearchShelfActionsModel.DetailsShelfActions -> {
                        runShelfActionsAnimation(2)
                        setShelfActionsForDetails(model.userWord)
                    }
                    is SearchShelfActionsModel.ListShelfActions -> {
                        runShelfActionsAnimation(if (model.hasFilter) 0 else 1)
                        setShelfActionsForList()
                    }
                    is SearchShelfActionsModel.None -> runShelfActionsAnimation(0)
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

        // Hide filter action if contextual sheet is expanded
        (activity as MainActivity).contextualSheetCallback.addOnSlideAction { _, offset ->
            val currentDest = navigator.currentDestination.value
                ?: Destination.HOME
            if (currentDest == Destination.TRENDING) {
                setSheetSlideOffsetForActions(
                        (requireActivity() as MainActivity).searchSheetCallback.currentSlide,
                        offset
                )
            }
        }
    }

    private fun setSheetSlideOffsetForActions(searchOffset: Float, contextualOffset: Float) {
        val zeroActions = numberOfShelfActionsShowing == 0
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val keyline3 = resources.getDimensionPixelSize(R.dimen.keyline_3)
        val hiddenMargin = keyline2
        val showingMargin = (((binding.actionOneImageView.width + keyline3) * numberOfShelfActionsShowing) +
            (if (zeroActions) keyline2 else 0)) + keyline2
        val params = binding.searchContainer.layoutParams  as ConstraintLayout.LayoutParams

        params.rightMargin = MathUtils.normalize(
            Math.max(searchOffset, contextualOffset),
            0F,
            1F,
            showingMargin.toFloat(),
            hiddenMargin.toFloat()
        ).toInt()
        binding.searchContainer.layoutParams = params
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
        if (!isAdded) return

        numberOfShelfActionsShowing = numberOfActions
        val zeroActions = numberOfActions == 0
        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val keyline3 = resources.getDimensionPixelSize(R.dimen.keyline_3)
        val showMargin = (((binding.actionOneImageView.width + keyline3) * numberOfActions) +
            (if (zeroActions) keyline2 else 0)) + keyline2
        val currentMargin =
            (binding.searchContainer.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if ((!zeroActions && currentMargin == showMargin)
                || (zeroActions && currentMargin == keyline2)) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = binding.searchContainer.layoutParams as ConstraintLayout.LayoutParams
                params.rightMargin = MathUtils.normalize(
                    interpolatedTime,
                    0F,
                    1F,
                    currentMargin.toFloat(),
                    showMargin.toFloat()
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
    private fun setShelfActionsForDetails(userWord: UserWord?) {
        if (userWord == null)  return

        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)

        binding.actionOneImageView.setOnClickListener {
            sharedViewModel.setCurrentWordFavorited(!isFavorited)
        }

        // TODO create an AVD
        binding.actionOneImageView.swapImageResource(if (isFavorited) {
            R.drawable.ic_round_favorite_24px
        } else {
            R.drawable.ic_round_favorite_border_24px
        })

        // TODO add share button setup
    }

    private fun setShelfActionsForList() {
        binding.actionOneImageView.setOnClickListener {
            sharedViewModel.onShouldOpenContextualFragment()
        }
        binding.actionOneImageView.swapImageResource(R.drawable.ic_round_filter_list_24px)
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

