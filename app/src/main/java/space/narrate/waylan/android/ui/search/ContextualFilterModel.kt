package space.narrate.waylan.android.ui.search

import space.narrate.waylan.android.Navigator

data class ContextualFilterModel(
    val destination: Navigator.Destination,
    val filter: List<Period>,
    val isFilterable: Boolean
)
