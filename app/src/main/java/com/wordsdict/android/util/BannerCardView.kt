package com.wordsdict.android.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.wordsdict.android.R
import kotlinx.android.synthetic.main.banner_card_veiw_layout.view.*

class BannerCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
        ) : MaterialCardView(context, attrs, defStyleAttr) {

    private var bannerText: String = ""
    private var bannerTopButtonText: String? = null
    private var bannerBottomButtonText: String? = null

    init {
        // get custom attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.BannerCardView, 0, 0)

        if (a != null) {
            if (a.hasValue(R.styleable.BannerCardView_bannerText)) {
                bannerText = a.getString(R.styleable.BannerCardView_bannerText) ?: bannerText
            }
            if (a.hasValue(R.styleable.BannerCardView_bannerTopButtonText)) {
                bannerTopButtonText = a.getString(R.styleable.BannerCardView_bannerTopButtonText)
            }
            if (a.hasValue(R.styleable.BannerCardView_bannerBottomButtonText)) {
                bannerBottomButtonText = a.getString(R.styleable.BannerCardView_bannerBottomButtonText)
            }
        }

        a?.recycle()


        // inflate compound view layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.banner_card_veiw_layout, this, true)

        //set defaults
        setBannerText(bannerText)
        setBannerTopButton(bannerTopButtonText, null)
        setBannerBottomButton(bannerBottomButtonText, null)
    }

    fun setBannerText(text: String) {
        bannerText = text
        bannerTextView.text = text
    }

    fun setBannerTopButton(text: String?, listener: OnClickListener?) {
        setBannerTopButtonText(text)
        setBannerTopButtonOnClickListener(listener)
    }

    fun setBannerTopButtonText(text: String?) {
        bannerTopButtonText = text
        if (text != null) {
            bannerTopButton.text = text
            bannerTopButton.visible()
        } else {
            bannerTopButton.text = ""
            bannerTopButton.gone()
        }
    }

    fun setBannerTopButtonOnClickListener(listener: OnClickListener?) {
        bannerTopButton.setOnClickListener(listener)
    }

    fun setBannerBottomButton(text: String?, listener: OnClickListener?) {
        setBannerBottomButtonText(text)
        setBannerBottomButtonOnClickListener(listener)
    }

    fun setBannerBottomButtonText(text: String?) {
        bannerBottomButtonText = text
        if (text != null) {
            bannerBottomButton.text = text
            bannerBottomButton.visible()
        } else {
            bannerBottomButton.text = ""
            bannerBottomButton.gone()
        }
    }

    fun setBannerBottomButtonOnClickListener(listener: OnClickListener?) {
        bannerBottomButton.setOnClickListener(listener)
    }

}