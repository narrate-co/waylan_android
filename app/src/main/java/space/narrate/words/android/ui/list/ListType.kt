package space.narrate.words.android.ui.list

import space.narrate.words.android.Navigator

enum class ListType(
        val fragmentTag: String,
        val title: String,
        val homeDestination: Navigator.Destination
) {
    TRENDING("trending_fragment_tag", "Trending", Navigator.Destination.TRENDING),
    RECENT("recent_fragment_tag", "Recent", Navigator.Destination.RECENT),
    FAVORITE("favorite_fragment_tag", "Favorite", Navigator.Destination.FAVORITE)
}