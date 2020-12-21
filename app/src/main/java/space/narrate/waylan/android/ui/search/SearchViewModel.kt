package space.narrate.waylan.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.android.R
import space.narrate.waylan.android.ui.search.ShelfActionModel.*
import space.narrate.waylan.android.util.SoftInputModel
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.data.prefs.RotationManager
import space.narrate.waylan.core.data.prefs.RotationUtils
import space.narrate.waylan.core.repo.AnalyticsRepository
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.util.MergedLiveData
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.switchMapTransform

/**
 * A ViewModel for [SearchFragment]
 */
class SearchViewModel(
    private val wordRepository: WordRepository,
    private val userRepository: UserRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val navigator: Navigator
): ViewModel(), RotationManager.Observer, RotationManager.PatternObserver {

    private val searchInput: MutableLiveData<String> = MutableLiveData()

    val searchResults: LiveData<List<SearchItemModel>> = searchInput
        .switchMapTransform { if (it.isEmpty()) getRecent() else getSearch(it) }
        .mapTransform { if (it.isEmpty()) addHeader(it) else it }

    val searchShelfRowModel: LiveData<SearchShelfActionRowModel>
        get() = SearchShelfActionRowLiveData(
            navigator.currentDestination,
            currentUserWord,
            userRepository.trendingListFilterLive,
            _searchSheetOffset,
            _searchSheetState,
            _contextualSheetOffset,
            _contextualSheetState,
            _softInputModel
        )

    private val _currentWord: MutableLiveData<String> = MutableLiveData()

    private val currentUserWord: LiveData<UserWord>
        get() = _currentWord.switchMapTransform { wordRepository.getUserWord(it) }

    private val _softInputModel: MutableLiveData<SoftInputModel> = MutableLiveData()
    private val _searchSheetOffset: MutableLiveData<Float> = MutableLiveData()
    private val _searchSheetState: MutableLiveData<Int> = MutableLiveData()
    private val _contextualSheetOffset: MutableLiveData<Float> = MutableLiveData()
    private val _contextualSheetState: MutableLiveData<Int> = MutableLiveData()

    private val _shouldCloseSheet: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldCloseSheet: LiveData<Event<Boolean>>
        get() = _shouldCloseSheet

    private val _shouldCloseKeyboard: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldCloseKeyboard: LiveData<Event<Boolean>>
        get() = _shouldCloseKeyboard

    private val _shouldOpenContextualSheet: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldOpenContextualSheet: LiveData<Event<Boolean>>
        get() = _shouldOpenContextualSheet

    private val _keyboardHeight: MutableLiveData<Float> = MutableLiveData()
    val keyboardHeight: LiveData<Float>
        get() = _keyboardHeight

    private val _shouldShowDetails: MutableLiveData<Event<String>> = MutableLiveData()
    val shouldShowDetails: LiveData<Event<String>>
        get() = _shouldShowDetails

    private val _shouldShowSettings: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldShowSettings: LiveData<Event<Boolean>>
        get() = _shouldShowSettings

    private val _shouldShowOrientationPrompt: MutableLiveData<Event<OrientationPromptModel>>
        = MutableLiveData()
    val shouldShowOrientationPrompt: LiveData<Event<OrientationPromptModel>>
        get() = _shouldShowOrientationPrompt

    init {
        searchInput.value = ""
    }

    private fun getRecent(): LiveData<List<SearchItemModel>> {
        return wordRepository.getUserWordRecents(25L)
            .mapTransform { recents -> recents.map { SearchItemModel.UserWordModel(it) } }
    }

    private fun getSearch(input: String): LiveData<List<SearchItemModel>> {
        return MergedLiveData(
            wordRepository.getWordsetWords(input),
            wordRepository.getSuggestItems(input)
        ) { words, suggestions ->
            val wordsModels = words.map { SearchItemModel.WordModel(it) }
            val suggestModels = suggestions.map { SearchItemModel.SuggestModel(it) }
            (wordsModels + suggestModels).distinctBy { item ->
                when (item) {
                    is SearchItemModel.WordModel -> item.word.word
                    is SearchItemModel.SuggestModel -> item.suggestItem.term
                    else -> ""
                }
            }
        }
    }

    private fun addHeader(list: List<SearchItemModel>): List<SearchItemModel> {
        return if (list.isEmpty()) {
            list.toMutableList().apply {
                add(0, SearchItemModel.HeaderModel(
                    R.string.search_banner_body
                ))
            }
        } else {
            list
        }
    }

    fun onCurrentWordChanged(word: String) {
        _currentWord.value = word
    }

    fun onSoftInputChanged(model: SoftInputModel) {
        _softInputModel.value = model
        _keyboardHeight.value = model.height.toFloat()
    }

    fun onSearchSheetOffsetChanged(offset: Float) {
        _searchSheetOffset.value = offset
    }

    fun onSearchSheetStateChanged(state: Int) {
        _searchSheetState.value = state
    }

    fun onContextualSheetOffsetChanged(offset: Float) {
        _contextualSheetOffset.value = offset
    }

    fun onContextualSheetStateChanged(state: Int) {
        _contextualSheetState.value = state
    }

    fun onSearchInputTextChanged(input: CharSequence?) {
        searchInput.value = input?.toString() ?: ""
    }

    fun onShelfActionClicked(action: ShelfActionModel) {
        when (action) {
            is ShareAction -> { /* Do nothing */ }
            is FavoriteAction -> setUserWordFavorited(true)
            is UnfavoriteAction -> setUserWordFavorited(false)
            is FilterAction -> _shouldOpenContextualSheet.value = Event(true)
            is CloseSheetAction -> _shouldCloseSheet.value = Event(true)
            is CloseKeyboardAction -> _shouldCloseKeyboard.value = Event(true)
            is SettingsAction -> _shouldShowSettings.value = Event(true)
        }
    }

    private fun setUserWordFavorited(newFavoriteValue: Boolean) {
        val id = _currentWord.value ?: return
        wordRepository.setUserWordFavorite(id, newFavoriteValue)
    }

    fun onWordClicked(item: SearchItemModel) {
        val word = when (item) {
            is SearchItemModel.WordModel -> item.word.word
            is SearchItemModel.UserWordModel -> item.userWord.word
            is SearchItemModel.SuggestModel -> item.suggestItem.term
            else -> return
        }

        analyticsRepository.logSearchWordEvent(word, word, item::class.java.simpleName)
        _shouldShowDetails.value = Event(word)
    }

    /**
     * Set the users [Orientation] preference. This preferenced is watched by the app, which will
     * send a broadcast to be received by all relevant components (active Activities) which will
     * handle calling the appropriate Activity methods.
     *
     * @param orientation The orientation the app should be set to
     */
    fun onOrientationPromptClicked(orientationPrompt: OrientationPromptModel) {
        userRepository.orientationLock = orientationPrompt.orientationToRequest
    }


    // RotationManager callbacks

    override fun onLockedRotate(
        old: RotationManager.RotationEvent,
        new: RotationManager.RotationEvent,
        lockedTo: Int
    ) {
        //do nothing
    }

    override fun onUnlockedOrientationChange(
        old: RotationManager.RotationEvent,
        new: RotationManager.RotationEvent
    ) {
        if (RotationUtils.isPortraitToLandscape(old, new)) {
            userRepository.portraitToLandscapeOrientationChangeCount++
            if (userRepository.portraitToLandscapeOrientationChangeCount == 2L) {
                _shouldShowOrientationPrompt.value = Event(OrientationPromptModel.LockToLandscape(
                    Orientation.fromActivityInfoScreenOrientation(new.orientation)
                ))
            }
        } else if (RotationUtils.isLandscapeToPortrait(old, new)) {
            userRepository.landscapeToPortraitOrientationChangeCount++
            if (userRepository.landscapeToPortraitOrientationChangeCount == 1L) {
                _shouldShowOrientationPrompt.value = Event(OrientationPromptModel.LockToPortrait(
                    Orientation.fromActivityInfoScreenOrientation(new.orientation)
                ))
            }
        }
    }

    override fun onLockedRotatePatternSeen(
        pattern: List<RotationManager.RotationEvent>,
        lockedTo: Int,
        observedSince: Long
    ) {
        if (RotationUtils.isLikelyUnlockDesiredScenario(
                        pattern,
                        lockedTo,
                        observedSince,
                        System.nanoTime()
                )) {
            //should suggest unlocking
            _shouldShowOrientationPrompt.value = Event(OrientationPromptModel.UnlockOrientation(
                Orientation.UNSPECIFIED
            ))
        }
    }

    override fun onUnlockedOrientationPatternSeen(pattern: List<RotationManager.RotationEvent>) {
        //do nothing. maybe move to pattern matching over orientation change counts
    }
}

