package space.narrate.words.android

import androidx.lifecycle.*
import space.narrate.words.android.data.analytics.AnalyticsRepository
import space.narrate.words.android.data.analytics.NavigationMethod
import space.narrate.words.android.data.firestore.users.UserWord
import space.narrate.words.android.data.repository.UserRepository
import space.narrate.words.android.data.repository.WordRepository
import space.narrate.words.android.di.UserScope
import space.narrate.words.android.ui.Event
import space.narrate.words.android.ui.search.Period
import space.narrate.words.android.util.LiveDataUtils
import space.narrate.words.android.util.peekOrNull
import space.narrate.words.android.util.switchMapTransform
import java.util.*
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

    // An internal representation of MainActivity's current Fragment backstack. This is used
    // a) by [SearchFragment] when determining when it should show shelf actions and b) to better
    // track and understand user navigation patterns.
    // TODO move backStack into a BackStackViewModel or separate impl to hold a default
    // TODO implementation to be reused across other host Activity VieWModels
    private var backStack: MutableLiveData<Stack<Navigator.HomeDestination>> = MutableLiveData()

    // A backing field for the word (as it appears in the dictionary) which should currently be
    // displayed by [DetailsFragment]. This is used instead of alternatives like passing the word
    // via the fragment's arguments
    private val _currentWord: MutableLiveData<String> = MutableLiveData()
    val currentWord: LiveData<String>
        get() = _currentWord

    val currentUserWord: LiveData<UserWord>
        get() = currentWord.switchMapTransform { wordRepository.getUserWord(it) }

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

    /**
     * @return A LiveData object that represents the current Fragment backstack for [MainActivity]
     */
    fun getBackStack(): LiveData<Stack<Navigator.HomeDestination>> {
        return backStack
    }

    /**
     * Add [dest] to the internally maintained Fragment backstack for [MainActivity]
     */
    fun onNavigatedTo(dest: Navigator.HomeDestination) {
        val stack = backStack.value ?: Stack()
        stack.push(dest)
        backStack.value = stack
    }

    /**
     * Pop the last [Navigator.HomeDestination] from the internally maintained Fragment backstack
     * for [MainActivity]. This method also handles reporting back navigation methods depending on
     * the value of [unconsumedNavigationMethod].
     */
    fun onNavigatedFrom(unconsumedNavigationMethod: NavigationMethod?) {
        val stack = backStack.value

        if (unconsumedNavigationMethod != null) {
            // this nav is coming from either a nav_icon click or a drag_dismiss
            analyticsRepository.logNavigateBackEvent(stack.toString(), unconsumedNavigationMethod)
        } else {
            analyticsRepository.logNavigateBackEvent(stack.toString(), NavigationMethod.BACK_BUTTON)
        }


        stack?.pop()
        if (stack != null) backStack.value = stack
    }

    fun getCurrentListFilter(): List<Period> {
        val dest = backStack.value?.peekOrNull ?: Navigator.HomeDestination.HOME

        return when (dest) {
            Navigator.HomeDestination.TRENDING -> userRepository.trendingListFilter
            Navigator.HomeDestination.RECENT -> userRepository.recentsListFilter
            Navigator.HomeDestination.FAVORITE -> userRepository.favoritesListFilter
            else -> emptyList()
        }
    }

    fun getCurrentListFilterLive(): LiveData<List<Period>> {
        return Transformations.switchMap(getBackStack()) {
            val dest = it.peekOrNull ?: Navigator.HomeDestination.HOME
            when (dest) {
                Navigator.HomeDestination.TRENDING -> userRepository.trendingListFilterLive
                Navigator.HomeDestination.RECENT -> userRepository.recentsListFilterLive
                Navigator.HomeDestination.FAVORITE -> userRepository.favoritesListFilterLive
                else -> LiveDataUtils.empty()
            }
        }
    }


    fun setListFilter(filter: List<Period>) {
        val dest = backStack.value?.peekOrNull ?: Navigator.HomeDestination.HOME

        when (dest) {
            Navigator.HomeDestination.TRENDING -> userRepository.trendingListFilter = filter
            Navigator.HomeDestination.RECENT -> userRepository.recentsListFilter = filter
            Navigator.HomeDestination.FAVORITE -> userRepository.favoritesListFilter = filter
        }
    }

}
