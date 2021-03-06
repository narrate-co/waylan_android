package space.narrate.waylan.android.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.android.util.SoftInputModel
import space.narrate.waylan.android.util.hide
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.core.util.mapTransform
import space.narrate.waylan.core.util.maybeSet
import space.narrate.waylan.core.util.switchMapTransform

/**
 * A ViewModel owned by MainActivity, accessible by all its child Fragments, making data
 * passing/sharing easy.
 */
class MainViewModel(
    private val wordRepository: WordRepository,
    private val userRepository: UserRepository,
    private val navigator: Navigator
) : ViewModel() {


    // A backing field for the word (as it appears in the dictionary) which should currently be
    // displayed by [DetailsFragment]. This is used instead of alternatives like passing the word
    // via the fragment's arguments
    private val _currentWord: MutableLiveData<String> = MutableLiveData()
    val currentWord: LiveData<String>
        get() = _currentWord

    val nightMode: LiveData<NightMode>
        get() = userRepository.nightModeLive

    val orientation: LiveData<Orientation>
        get() = userRepository.orientationLockLive

    private val _shouldShowDetails: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldShowDetails: LiveData<Event<Boolean>>
        get() = _shouldShowDetails

    private val _shouldOpenAndFocusSearch: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldOpenAndFocusSearch: LiveData<Event<Boolean>>
        get() = _shouldOpenAndFocusSearch

    private val _shouldOpenContextualSheet: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldOpenContextualSheet: LiveData<Event<Boolean>>
        get() = _shouldOpenContextualSheet

    private val _shouldHideBottomSheets: MutableLiveData<Boolean> = MutableLiveData()
    val shouldHideBottomSheets: LiveData<Boolean>
        get() = _shouldHideBottomSheets

    private val _shouldHideFloatingNavigationBar: MutableLiveData<Boolean> = MutableLiveData()
    val shouldHideFloatingNavigationBar: LiveData<Boolean>
        get() = _shouldHideFloatingNavigationBar

    private val _softInputModel: MutableLiveData<SoftInputModel> = MutableLiveData()

    val softInputModel: LiveData<SoftInputModel>
        get() = _softInputModel

    val keyboardHeight: LiveData<Float>
        get() = _softInputModel.mapTransform { it.height.toFloat() }

    fun onProcessText(textToProcess: String?) {
        if (!textToProcess.isNullOrBlank()) {
            onChangeCurrentWord(textToProcess)
            _shouldShowDetails.value = Event(true)
        }
    }

    fun onShouldOpenAndFocusSearch() {
        _shouldOpenAndFocusSearch.value = Event(true)
    }

    fun onShouldOpenContextualFragment() {
        _shouldOpenContextualSheet.value = Event(true)
    }

    /**
     * Set the current word to be displayed by [DetailsFragment]
     *
     * @param word The word (as it appears in the dictionary to be defined in [DetailsFragment]
     */
    fun onChangeCurrentWord(word: String) {
        val sanitizedId = word.toLowerCase()
        _currentWord.value = sanitizedId
        setCurrentWordRecented()
    }

    fun onListFilterPeriodClicked(period: Period) {
        setListFilter(listOf(period))
    }

    fun onClearListFilter() {
        setListFilter(emptyList())
    }

    fun onSoftInputChanged(model: SoftInputModel) {
      _softInputModel.value = model
    }

    fun onContextualSheetHidden() {
        onClearListFilter()
    }

    /**
     * Add [currentWord]'s value (a word as it appears in the dictionary) to the current user's
     * list of favorited words.
     *
     * This adds [UserWordType.FAVORITED] to this user's [UserWord.types] for the [currentWord].
     */
    fun setCurrentWordFavorited(favorite: Boolean) {
        val id = _currentWord.value ?: return
        wordRepository.setUserWordFavorite(id, favorite)
    }

    fun onCurrentDestinationChanged(destination: Destination) {
        when (destination) {
            Destination.SETTINGS,
            Destination.ADD_ONS,
            Destination.ABOUT,
            Destination.THIRD_PARTY,
            Destination.DEV_SETTINGS -> {
                _shouldHideFloatingNavigationBar.maybeSet(true)
                _shouldHideBottomSheets.maybeSet(true)
            }
            Destination.DETAILS -> {
                _shouldHideFloatingNavigationBar.maybeSet(true)
                _shouldHideBottomSheets.maybeSet(false)
            }
            else -> {
                _shouldHideFloatingNavigationBar.maybeSet(false)
                _shouldHideBottomSheets.maybeSet(false)
            }
        }
    }

    /**
     * Add [currentWord]'s value (a word as it appears in the dictionary) to the current user's
     * list of recently viewed words.
     *
     * This adds [UserWordType.RECENT] to this user's [UserWord.types] for the [currentWord].
     */
    private fun setCurrentWordRecented() {
        val id = _currentWord.value ?: return
        wordRepository.setUserWordRecent(id)
    }


    private fun setListFilter(filter: List<Period>) {
        val dest = navigator.currentDestination.value ?: Destination.TRENDING

        when (dest) {
            Destination.TRENDING -> userRepository.trendingListFilter = filter
            Destination.RECENT -> userRepository.recentsListFilter = filter
            Destination.FAVORITE -> userRepository.favoritesListFilter = filter
        }
    }
}
