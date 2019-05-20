package space.narrate.words.android.ui.list

import space.narrate.words.android.R

enum class ListType(
        val titleRes: Int
) {
    TRENDING(R.string.title_trending),
    RECENT(R.string.title_recent),
    FAVORITE(R.string.title_favorite)
}