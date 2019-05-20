package space.narrate.words.android

import androidx.lifecycle.*
import space.narrate.words.android.data.repository.AnalyticsRepository
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.prefs.NightMode
import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.data.prefs.ThirdPartyLibrary
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.di.UserScope
import space.narrate.words.android.ui.Event
import space.narrate.words.android.ui.search.ContextualFilterModel
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.ui.search.SearchShelfActionsModel
import space.narrate.words.android.util.switchMapTransform
import javax.inject.Inject

/**
 * A ViewModel owned by MainActivity, accessible by all its child Fragments, making data
 * passing/sharing easy.
 */
@UserScope
class MainViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentDestination: MutableLiveData<Navigator.Destination> = MutableLiveData()
    val currentDestination: LiveData<Navigator.Destination>
        get() = _currentDestination

    val searchShelfModel: LiveData<SearchShelfActionsModel> = currentDestination
        .switchMapTransform { dest ->
            val result = MediatorLiveData<SearchShelfActionsModel>()
            when (dest) {
                Navigator.Destination.DETAILS -> result.addSource(currentUserWord) {
                    result.value = SearchShelfActionsModel.DetailsShelfActions(it)
                }
                Navigator.Destination.TRENDING -> result.addSource(
                    userRepository.trendingListFilterLive
                ) {
                    result.value = SearchShelfActionsModel.ListShelfActions(it.isNotEmpty())
                }
                else -> result.value = SearchShelfActionsModel.None
            }

            result
        }

    val contextualFilterModel: LiveData<ContextualFilterModel> = currentDestination
        .switchMapTransform { dest ->
            val result = MediatorLiveData<ContextualFilterModel>()

            result.addSource(
                when (dest) {
                    Navigator.Destination.TRENDING -> userRepository.trendingListFilterLive
                    Navigator.Destination.RECENT -> userRepository.recentsListFilterLive
                    Navigator.Destination.FAVORITE -> userRepository.favoritesListFilterLive
                    else -> {
                        // TODO : Clean up
                        val data = MutableLiveData<List<Period>>()
                        data.value = emptyList()
                        data
                    }
                }
            ) { filter ->
                result.value = ContextualFilterModel(
                    dest,
                    filter,
                    dest == Navigator.Destination.TRENDING
                )
            }

            result
        }

    // A backing field for the word (as it appears in the dictionary) which should currently be
    // displayed by [DetailsFragment]. This is used instead of alternatives like passing the word
    // via the fragment's arguments
    private val _currentWord: MutableLiveData<String> = MutableLiveData()
    val currentWord: LiveData<String>
        get() = _currentWord

    private val currentUserWord: LiveData<UserWord>
        get() = currentWord.switchMapTransform { wordRepository.getUserWord(it) }

    val nightMode: LiveData<NightMode>
        get() = userRepository.nightModeLive

    val orientation: LiveData<Orientation>
        get() = userRepository.orientationLockLive

    val thirdPartyLibraries: List<ThirdPartyLibrary>
        get() = userRepository.allThirdPartyLibraries

    private val _shouldNavigateBack: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldNavigateBack: LiveData<Event<Boolean>>
        get() = _shouldNavigateBack

    private val _shouldShowDetails: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldShowDetails: LiveData<Event<Boolean>>
        get() = _shouldShowDetails

    private val _shouldOpenAndFocusSearch: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldOpenAndFocusSearch: LiveData<Event<Boolean>>
        get() = _shouldOpenAndFocusSearch

    private val _shouldOpenContextualSheet: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val shouldOpenContextualSheet: LiveData<Event<Boolean>>
        get() = _shouldOpenContextualSheet

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

    fun onNavigationIconClicked(currentDestination: String): Boolean {
        analyticsRepository.logNavigationIconEvent(currentDestination)
        _shouldNavigateBack.value = Event(true)
        return true
    }

    fun onDragDismissBackEvent(currentDestination: String): Boolean {
        analyticsRepository.logDragDismissEvent(currentDestination)
        _shouldNavigateBack.value = Event(true)
        return true
    }

    fun onListFilterPeriodClicked(period: Period) {
        setListFilter(listOf(period))
    }

    fun onClearListFilter() {
        setListFilter(emptyList())
    }

    fun onContextualSheetHidden() {
        onClearListFilter()
    }

    fun onDestinationChanged(destination: Navigator.Destination) {
        _currentDestination.value = destination
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
        val dest = currentDestination.value ?: Navigator.Destination.HOME

        when (dest) {
            Navigator.Destination.TRENDING -> userRepository.trendingListFilter = filter
            Navigator.Destination.RECENT -> userRepository.recentsListFilter = filter
            Navigator.Destination.FAVORITE -> userRepository.favoritesListFilter = filter
        }
    }

}
