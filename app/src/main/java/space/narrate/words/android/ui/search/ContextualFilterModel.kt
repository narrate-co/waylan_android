package space.narrate.words.android.ui.search

import space.narrate.words.android.Navigator

data class ContextualFilterModel(
    val destination: Navigator.Destination,
    val filter: List<Period>,
    val isFilterable: Boolean
)
