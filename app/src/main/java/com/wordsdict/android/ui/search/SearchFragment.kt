package com.wordsdict.android.ui.search

import android.animation.*
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Property
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wordsdict.android.*
import com.wordsdict.android.ui.common.BaseUserFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.wordsdict.android.data.firestore.users.UserWord
import com.wordsdict.android.data.firestore.users.UserWordType
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.SimpleWordSource
import com.wordsdict.android.data.repository.SuggestSource
import com.wordsdict.android.data.repository.WordSource
import com.wordsdict.android.ui.settings.ShelfTransition
import com.wordsdict.android.util.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp

class SearchFragment : BaseUserFragment(), SearchAdapter.WordAdapterHandlers, TextWatcher{

    companion object {
        fun newInstance() = SearchFragment()
        const val TAG = "SearchFragment"
    }

    private val viewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(SearchViewModel::class.java)
    }

    private val sharedViewModel by lazy {
        ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainViewModel::class.java)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(view)
    }

    private val adapter by lazy { SearchAdapter(this) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)



        return view
    }

    override fun onEnterTransactionEnded() {

        setUpSearchBar(view)

        setUpRecyclerView(view)

        setUpShelfActions(view)

        sharedViewModel.currentWord.observe(this, Observer {
            viewModel.setWordId(it)
        })

        viewModel.firestoreUserSource.observe(this, Observer { source ->
            setUserWord(source.userWord)
        })
    }

    private fun setUpSearchBar(view: View?) {
        if (view == null) return

        view.searchEditText.addTextChangedListener(this)

        view.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                bottomSheetBehavior.expand()
            } else {
                bottomSheetBehavior.collapse(activity)
            }
        }

        view.searchEditText.setOnClickListener {
            if (searchEditText.hasFocus()) {
                bottomSheetBehavior.expand()
            }
        }


    }

    private fun setUpRecyclerView(view: View?) {
        if (view == null) return
        //set up recycler view
        view.recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        view.recycler.adapter = adapter

        // hide keyboard if scrolling search results
        view.recycler.setOnTouchListener { _, _ ->
            activity?.hideSoftKeyboard()
            false
        }

        viewModel.searchResults.observe(this, Observer {
            adapter.submitList(it)
            setBanner(it.isEmpty())
            view.recycler.scrollToPosition(0)
        })
    }

    private fun setBanner(isListEmpty: Boolean) {
        if (isListEmpty) {
            adapter.setHeader(Banner(getString(R.string.search_banner_body), null, null))
        } else {
            adapter.setHeader(null)
        }
    }

    private fun setUpShelfActions(view: View?) {
        if (view == null) return

        // Hide actions when not in details
        sharedViewModel.getBackStack().observe(this, Observer {
            val dest = if (it.empty()) Navigator.HomeDestination.HOME else it.peek()
            // wait for the next layout step to grantee the actions.width is correctly captured
            view.post {
                when (dest) {
                    Navigator.HomeDestination.HOME, Navigator.HomeDestination.LIST -> runActionsAnimation(false)
                    Navigator.HomeDestination.DETAILS -> runActionsAnimation(true)
                }
            }
        })

        // Hide actions if sheet is expanded
        (activity as MainActivity).searchSheetCallback.addOnSlideAction { view, offset ->
            val currentDest = sharedViewModel.getBackStack().value?.peek()
                    ?: Navigator.HomeDestination.HOME
            if (currentDest == Navigator.HomeDestination.DETAILS) {
                val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
                val hideMargin = keyline2
                val showMargin = actions.width + keyline2
                val params = search.layoutParams as ConstraintLayout.LayoutParams
                val adjustedInterpolatedTime = 1.0F - offset
                params.rightMargin = Math.max(hideMargin, (showMargin * adjustedInterpolatedTime).toInt())
                search.layoutParams = params
            }
        }

        view.share.setOnClickListener {
//            runShelfTransition()
            runShelfAnimation()
        }

    }

    fun focusAndOpenSearch() {
        bottomSheetBehavior.expand()
        searchEditText.requestFocus()
        activity?.showSoftKeyboard(searchEditText)
    }


    @Volatile
    var expanded = false

    fun runShelfTransition() {
        val changeBounds = ChangeBounds()
        changeBounds.interpolator = DecelerateInterpolator()
        changeBounds.duration = 200
        TransitionManager.beginDelayedTransition(view as ViewGroup, changeBounds)
        if (!expanded) {
            val smartSuggestion = layoutInflater.inflate(R.layout.smart_suggestion_item, shelfContainer, false)
            shelfContainer.addView(smartSuggestion)
            val display = activity!!.windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            smartSuggestion.measure(point.x, point.y)
            val measuredDiff = smartSuggestion.measuredHeight + smartSuggestion.marginTop + smartSuggestion.marginBottom
            bottomSheetBehavior.peekHeight += measuredDiff
        } else {
            shelfContainer.removeAllViews()
            val measuredDiff = shelfContainer.height
            bottomSheetBehavior.peekHeight -= measuredDiff
        }
        expanded = !expanded
    }

    fun runShelfAnimation() {
        if (!expanded) {
            addShelfItem()
        } else {
            removeShelfItem()
        }
    }

    private fun addShelfItem() {
        synchronized(expanded) {

            val smartSuggestion = layoutInflater.inflate(R.layout.smart_suggestion_item, shelfContainer, false)
            println("SearchFragment::runShelfAnimation - shelfHeight = ${shelfContainer.height}, smartSuggestionHeight = ${smartSuggestion.height}")
            val originalShelfHeight = shelfContainer.height
            val originalPeekHeight = bottomSheetBehavior.peekHeight
            val display = activity!!.windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            smartSuggestion.measure(point.x, point.y)
            val measuredDiff = smartSuggestion.measuredHeight + smartSuggestion.marginTop + smartSuggestion.marginBottom


            smartSuggestion.alpha = 0F
            smartSuggestion.gone()


            //alpha
            val alphaAnimator = ObjectAnimator.ofFloat(smartSuggestion, "alpha", 1.0F)
            alphaAnimator.setAutoCancel(true)
            alphaAnimator.doOnEnd { smartSuggestion.alpha = 1F }
            alphaAnimator.addUpdateListener {
                if (it.animatedFraction > .25F) {
                    smartSuggestion.visible()
                }
            }

            //peek
            val peekAnimator = ObjectAnimator.ofObject(view, object: Property<View, Int>(Int::class.java, "peek") {
                var behavior : BottomSheetBehavior<View>? = null
                override fun get(view: View?): Int? {
                    return null
                }

                override fun set(view: View, value: Int) {
                    if (behavior == null) {
                        behavior = BottomSheetBehavior.from(view)
                    }
                    behavior?.peekHeight = value
                }
            }, null, originalPeekHeight, originalPeekHeight + measuredDiff)
            peekAnimator.setAutoCancel(true)
            peekAnimator.doOnEnd {
                bottomSheetBehavior.peekHeight = originalPeekHeight + measuredDiff
            }

            //height
            val heightAnimator = ObjectAnimator.ofObject(shelfContainer, object: Property<View, Int>(Int::class.java, "shelfHeight") {
                override fun get(view: View?): Int? {
                    return null
                }

                override fun set(view: View, value: Int) {
                    view.layoutParams.height = value
                }
            }, null, originalShelfHeight, originalShelfHeight + measuredDiff)
            heightAnimator.setAutoCancel(true)
            heightAnimator.doOnStart {
                shelfContainer.addView(smartSuggestion)
            }
            heightAnimator.doOnEnd {
                shelfContainer.layoutParams.height = originalShelfHeight + measuredDiff
            }


            val set = AnimatorSet()
            set.duration = 200
            set.interpolator = DecelerateInterpolator()
            set.playTogether(alphaAnimator, peekAnimator, heightAnimator)
            heightAnimator.addListener(object: Animator.AnimatorListener {
                var canceled = false
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    println("onAnimationEnd")
                    launch(UI) {
                        delay(3000)
                        if (!canceled) {
                            removeShelfItem()
                        }
                    }
                }
                override fun onAnimationCancel(animation: Animator?) {
                    println("onAnimationCanceled")
                    canceled = true
                }
                override fun onAnimationStart(animation: Animator?) {}
            })
            set.start()
            expanded = true
        }
    }

    private fun removeShelfItem() {
        synchronized(expanded) {

            val originalShelfHeight = shelfContainer.height
            val originalPeekHeight = bottomSheetBehavior.peekHeight
            val smartSuggestion: View? = shelfContainer.getChildAt(0)
            val measuredDiff = originalShelfHeight

            println("SearchFragment::runShelfAnimation collapsing - shelfHeight = ${shelfContainer.height}")

            //alpha
            val alphaAnimator = ObjectAnimator.ofFloat(smartSuggestion, "alpha", 0.0F)
            alphaAnimator.setAutoCancel(true)
            alphaAnimator.doOnEnd {
                smartSuggestion?.alpha = 0F
            }

            //peek
            val peekAnimator = ObjectAnimator.ofObject(view, object: Property<View, Int>(Int::class.java, "peek") {
                var behavior : BottomSheetBehavior<View>? = null
                override fun get(view: View?): Int? {
                    return null
                }

                override fun set(view: View, value: Int) {
                    if (behavior == null) {
                        behavior = BottomSheetBehavior.from(view)
                    }
                    behavior?.peekHeight = value
                }
            }, null, originalPeekHeight, originalPeekHeight - measuredDiff)
            peekAnimator.setAutoCancel(true)
            peekAnimator.doOnEnd {
                bottomSheetBehavior.peekHeight = originalPeekHeight - measuredDiff
            }

            //height
            val heightAnimator = ObjectAnimator.ofObject(shelfContainer, object: Property<View, Int>(Int::class.java, "shelfHeight") {
                override fun get(view: View?): Int? {
                    return null
                }

                override fun set(view: View, value: Int) {
                    view.layoutParams.height = value
                }
            }, null, originalShelfHeight, originalShelfHeight - measuredDiff)
            heightAnimator.setAutoCancel(true)
            heightAnimator.doOnStart {
                shelfContainer.removeAllViews()
            }
            heightAnimator.doOnEnd {
                shelfContainer.layoutParams.height = originalShelfHeight - measuredDiff
            }


            val set = AnimatorSet()
            set.duration = 200
            set.interpolator = DecelerateInterpolator()
            set.playTogether(alphaAnimator, peekAnimator, heightAnimator)
            set.start()

            expanded = false
        }
    }

    private fun runActionsAnimation(show: Boolean) {
        if (!isAdded) return

        val keyline2 = resources.getDimensionPixelSize(R.dimen.keyline_2)
        val hideMargin = keyline2
        val showMargin = actions.width + keyline2
        val currentMargin = (search.layoutParams as ConstraintLayout.LayoutParams).rightMargin

        // don't animate if already shown or hidden
        if ((show && currentMargin == showMargin) || (!show && currentMargin == hideMargin)) return

        val animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                val params = search.layoutParams as ConstraintLayout.LayoutParams
                val adjustedInterpolatedTime: Float = if (show) interpolatedTime else (1.0F - interpolatedTime)
                params.rightMargin = Math.max(hideMargin, (showMargin * adjustedInterpolatedTime).toInt())
                search.layoutParams = params
            }
        }
        animation.duration = 200
        animation.interpolator = FastOutSlowInInterpolator()
        search.startAnimation(animation)
    }

    private fun setUserWord(userWord: UserWord?) {
        if (userWord == null)  return

        val isFavorited = userWord.types.containsKey(UserWordType.FAVORITED.name)

        favorite.setOnClickListener {
            sharedViewModel.setCurrentWordFavorited(!isFavorited)
        }

        favorite?.setImageResource(if (isFavorited) R.drawable.ic_round_favorite_24px else R.drawable.ic_round_favorite_border_24px)
    }


    override fun onWordClicked(word: WordSource) {
        bottomSheetBehavior.collapse(activity)
        val id = when (word) {
            is SimpleWordSource -> word.word.word
            is FirestoreUserSource -> word.userWord.word
            is SuggestSource -> word.item.term
            else -> ""
        }
        sharedViewModel.setCurrentWordId(id)
        viewModel.logSearchWordEvent(id, word)
        (activity as MainActivity).showDetails()
    }

    override fun onBannerClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerTopButtonClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun onBannerBottomButtonClicked(banner: Banner) {
        // do nothing. Search banner should not have any buttons
    }

    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        viewModel.searchInput = s?.toString() ?: ""
    }
}

