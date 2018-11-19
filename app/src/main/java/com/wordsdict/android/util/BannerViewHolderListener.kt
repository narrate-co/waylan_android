package com.wordsdict.android.util

import android.view.View

interface BannerViewHolder {
    fun setBanner(view: BannerCardView, banner: Banner?, listener: BannerViewHolderListener) {
        if (banner == null) return
        view.setBannerText(banner.text)
        view.setOnClickListener { listener.onBannerClicked(banner) }
        view.setBannerTopButton(banner.topButtonText, View.OnClickListener { listener.onBannerTopButtonClicked(banner) })
        view.setBannerBottomButton(banner.bottomButtonText, View.OnClickListener { listener.onBannerBottomButtonClicked(banner) })
    }
}

interface BannerViewHolderListener {
    fun onBannerClicked(banner: Banner)
    fun onBannerTopButtonClicked(banner: Banner)
    fun onBannerBottomButtonClicked(banner: Banner)
}
