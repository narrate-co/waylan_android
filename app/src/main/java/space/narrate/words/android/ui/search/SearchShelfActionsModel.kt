package space.narrate.words.android.ui.search

import space.narrate.words.android.data.firestore.users.UserWord

sealed class SearchShelfActionsModel {

    class DetailsShelfActions(val userWord: UserWord) : SearchShelfActionsModel()

    class ListShelfActions(val hasFilter: Boolean) : SearchShelfActionsModel()

    object None : SearchShelfActionsModel()
}

