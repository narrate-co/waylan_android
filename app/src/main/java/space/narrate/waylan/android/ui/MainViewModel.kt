package space.narrate.waylan.android.ui

import androidx.lifecycle.*
import space.narrate.waylan.core.data.firestore.users.UserWord
import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.core.data.repo.UserRepository
import space.narrate.waylan.core.data.repo.WordRepository
import space.narrate.waylan.core.ui.common.Event
import space.narrate.waylan.android.ui.search.ContextualFilterModel
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.android.ui.search.SearchShelfActionsModel
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
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

    val searchShelfModel: LiveData<SearchShelfActionsModel> = navigator.currentDestination
        .switchMapTransform { dest ->
            val result = MediatorLiveData<SearchShelfActionsModel>()
            when (dest) {
                Destination.DETAILS -> result.addSource(currentUserWord) {
                    result.value = SearchShelfActionsModel.DetailsShelfActions(it)
                }
                Destination.TRENDING -> result.addSource(
                    userRepository.trendingListFilterLive
                ) {
                    result.value = SearchShelfActionsModel.ListShelfActions(it.isNotEmpty())
                }
                else -> result.value = SearchShelfActionsModel.None
            }

            result
        }

    val contextualFilterModel: LiveData<ContextualFilterModel> = navigator.currentDestination
        .switchMapTransform { dest ->
            val result = MediatorLiveData<ContextualFilterModel>()

            result.addSource(
                when (dest) {
                    Destination.TRENDING -> userRepository.trendingListFilterLive
                    Destination.RECENT -> userRepository.recentsListFilterLive
                    Destination.FAVORITE -> userRepository.favoritesListFilterLive
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
                    dest == Destination.TRENDING
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

    fun onListFilterPeriodClicked(period: Period) {
        setListFilter(listOf(period))
    }

    fun onClearListFilter() {
        setListFilter(emptyList())
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
        val dest = navigator.currentDestination.value ?: Destination.HOME

        when (dest) {
            Destination.TRENDING -> userRepository.trendingListFilter = filter
            Destination.RECENT -> userRepository.recentsListFilter = filter
            Destination.FAVORITE -> userRepository.favoritesListFilter = filter
        }
    }
}
