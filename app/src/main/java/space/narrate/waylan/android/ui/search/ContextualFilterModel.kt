package space.narrate.waylan.android.ui.search

import space.narrate.waylan.core.data.firestore.Period
import space.narrate.waylan.core.ui.Destination

data class ContextualFilterModel(
    val destination: Destination,
    val filter: List<Period>,
    val isFilterable: Boolean
)
