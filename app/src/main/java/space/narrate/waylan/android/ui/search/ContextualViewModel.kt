package space.narrate.waylan.android.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.repo.UserRepository
import space.narrate.waylan.core.ui.Destination
import space.narrate.waylan.core.ui.Navigator
import space.narrate.waylan.core.util.switchMapTransform
import space.narrate.waylan.core.util.toLiveData

class ContextualViewModel(
    private val userRepository: UserRepository,
    private val navigator: Navigator
) : ViewModel() {

    val contextualFilterModel: LiveData<ContextualFilterModel> = navigator.currentDestination
        .switchMapTransform { dest ->
            val result = MediatorLiveData<ContextualFilterModel>()

            result.addSource(
                when (dest) {
                    Destination.TRENDING -> userRepository.trendingListFilterLive
                    Destination.RECENT -> userRepository.recentsListFilterLive
                    Destination.FAVORITE -> userRepository.favoritesListFilterLive
                    else -> emptyList<Period>().toLiveData
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
}