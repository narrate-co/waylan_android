package space.narrate.waylan.android.ui.list

import space.narrate.waylan.android.R

enum class ListType(
        val titleRes: Int
) {
    TRENDING(R.string.title_trending),
    RECENT(R.string.title_recent),
    FAVORITE(R.string.title_favorite)
}