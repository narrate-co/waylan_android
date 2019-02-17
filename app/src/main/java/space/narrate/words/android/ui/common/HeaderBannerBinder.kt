package space.narrate.words.android.ui.common

import android.view.View
import space.narrate.words.android.util.widget.BannerCardView

/**
 * An interface with a provided implementation to handle binding a [HeaderBanner] to
 * a [BannerCardView].
 *
 * @see [ListHeaderViewHolder] and [SearchHeaderViewHolder]
 */
interface HeaderBannerBinder {
    fun bindHeaderBanner(view: BannerCardView, banner: HeaderBanner?, listener: HeaderBannerListener) {
        if (banner == null) return
        view.setBannerText(banner.text)
        view.setOnClickListener { listener.onBannerClicked(banner) }
        view.setBannerTopButton(banner.topButtonText, View.OnClickListener { listener.onBannerTopButtonClicked(banner) })
        view.setBannerBottomButton(banner.bottomButtonText, View.OnClickListener { listener.onBannerBottomButtonClicked(banner) })
    }
}

