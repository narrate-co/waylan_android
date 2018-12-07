package com.wordsdict.android.ui.common

/**
 * A data object that represents data to be set on [BannerCardView] when used as the first item
 * in a RecyclerView.
 *
 * @see ListTypeAdapter
 * @see SearchAdapter
 *
 * @property text The main body of the banner
 * @property topButtonText The text of the top button (aligned vertically, below [text], above
 *  [bottomButtonText] and along the right side of the parent)
 * @property bottomButtonText The text of the bottom button (aligned vertically below
 *  [topButtonText], along the right side of the parent)
 * @property label A short text label (placed above [text] and aligned along the left side
 *  of the parent)
 */
data class HeaderBanner(
        val text: String,
        val topButtonText: String? = null,
        val bottomButtonText: String? = null,
        val label: String? = null
)