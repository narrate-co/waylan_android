package com.wordsdict.android

import androidx.lifecycle.*
import com.wordsdict.android.data.analytics.AnalyticsRepository
import com.wordsdict.android.data.analytics.NavigationMethod
import com.wordsdict.android.data.repository.FirestoreUserSource
import com.wordsdict.android.data.repository.WordRepository
import com.wordsdict.android.di.UserScope
import java.util.*
import javax.inject.Inject

/**
 * A ViewModel owned by MainActivity, accessible by all its child Fragments, making data
 * passing/sharing easy.
 */
@UserScope
class MainViewModel @Inject constructor(
        private val wordRepository: WordRepository,
        private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    // An internal representation of MainActivity's current Fragment backstack. This is used
    // a) by [SearchFragment] when determining when it should show shelf actions and b) to better
    // track and understand user navigation patterns.
    // TODO move backStack into a BackStackViewModel to hold a default implementation to be reused
    // TODO across other host Activity VieWModels
    private var backStack: MutableLiveData<Stack<Navigator.HomeDestination>> = MutableLiveData()

    // A backing field for the word (as it appears in the dictionary) which should currently be
    // displayed by [DetailsFragment]. This is used instead of alternatives like passing the word
    // via the fragment's arguments
    private var _currentWord = MutableLiveData<String>()

    val currentWord: LiveData<String> = _currentWord

    /**
     * Get a LiveData which observes [currentWord]'s [FirestoreUserSource].
     *
     * @see SearchFragment for how this is used to set the correct shelf action states
     */
    fun getCurrentFirestoreUserWord(): LiveData<FirestoreUserSource> =
            Transformations.switchMap(currentWord) {
                wordRepository.getFirestoreUserSource(it)
            }

    /**
     * Set the current word to be displayed by [DetailsFragment]
     *
     * @param word The word (as it appears in the dictionary to be defined in [DetailsFragment]
     */
    fun setCurrentWord(word: String) {
        val sanitizedId = word.toLowerCase()
        _currentWord.value = sanitizedId
    }

    /**
     * Add [currentWord]'s value (a word as it appears in the dictionary) to the current user's
     * list of favorited words.
     *
     * This adds [UserWordType.FAVORITED] to this user's [UserWord.types] for the [currentWord].
     */
    fun setCurrentWordFavorited(favorite: Boolean) {
        val id = _currentWord.value ?: return
        wordRepository.setFavorite(id, favorite)
    }

    /**
     * Add [currentWord]'s value (a word as it appears in the dictionary) to the current user's
     * list of recently viewed words.
     *
     * This adds [UserWordType.RECENT] to this user's [UserWord.types] for the [currentWord].
     */
    fun setCurrentWordRecented() {
        val id = _currentWord.value ?: return
        wordRepository.setRecent(id)
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
    fun pushToBackStack(dest: Navigator.HomeDestination) {
        val stack = backStack.value ?: Stack()
        stack.push(dest)
        backStack.value = stack
    }

    /**
     * Pop the last [Navigator.HomeDestination] from the internally maintained Fragment backstack
     * for [MainActivity]. This method also handles reporting back navigation methods depending on
     * the value of [unconsumedNavigationMethod].
     */
    fun popBackStack(unconsumedNavigationMethod: NavigationMethod?) {
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

}
