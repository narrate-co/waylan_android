package com.wordsdict.android.ui.common


/**
 * A listener implemented by any [RecyclerView.Adapter] which contains a [HeaderBannerBinder]
 * as it's first item
 */
interface HeaderBannerListener {
    /**
     * The entire header view has been clicked
     */
    fun onBannerClicked(banner: HeaderBanner)

    /**
     * The banner's top button has been clicked
     */
    fun onBannerTopButtonClicked(banner: HeaderBanner)

    /**
     * The banner's bottom button has been clicked
     */
    fun onBannerBottomButtonClicked(banner: HeaderBanner)
}