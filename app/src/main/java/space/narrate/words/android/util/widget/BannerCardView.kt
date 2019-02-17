package space.narrate.words.android.util.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import space.narrate.words.android.R
import space.narrate.words.android.util.gone
import space.narrate.words.android.util.visible
import kotlinx.android.synthetic.main.banner_card_veiw_layout.view.*

/**
 * A composite [MaterialCardView] which encapsulates Banner logic
 *
 * XML properties include
 * @property bannerLabel The [HeaderBanner.label] to be displayed
 * @property bannerText The [HeaderBanner.text] to be displayed
 * @property bannerTopButtonText The [HeaderBanner.topButtonText] to be displayed
 * @property bannerBottomButtonText The [HeaderBanner.bottomButtonText] to be displayed
 *
 * Any parameters set to null by either not being set via an xml property or explicitly using one
 * of the classes helper functions, will not be shown. The only required parameter is [bannerText].
 */
class BannerCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
        ) : MaterialCardView(context, attrs, defStyleAttr) {

    private var bannerLabel: String? = null
    private var bannerText: String = ""
    private var bannerTopButtonText: String? = null
    private var bannerBottomButtonText: String? = null

    init {
        // get custom attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.BannerCardView, 0, 0)

        if (a != null) {
            if (a.hasValue(R.styleable.BannerCardView_bannerLabel)) {
                bannerLabel = a.getString(R.styleable.BannerCardView_bannerLabel)
            }
            if (a.hasValue(R.styleable.BannerCardView_bannerText)) {
                bannerText = a.getString(R.styleable.BannerCardView_bannerText) ?: bannerText
            }
            if (a.hasValue(R.styleable.BannerCardView_bannerTopButtonText)) {
                bannerTopButtonText = a.getString(R.styleable.BannerCardView_bannerTopButtonText)
            }
            if (a.hasValue(R.styleable.BannerCardView_bannerBottomButtonText)) {
                bannerBottomButtonText =
                        a.getString(R.styleable.BannerCardView_bannerBottomButtonText)
            }
        }

        a?.recycle()


        // inflate compound view layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.banner_card_veiw_layout, this, true)

        //set defaults
        setBannerLabel(bannerLabel, null)
        setBannerText(bannerText)
        setBannerTopButton(bannerTopButtonText, null)
        setBannerBottomButton(bannerBottomButtonText, null)
    }

    /**
     * Function to set all [HeaderBanner] properties at once.
     *
     * @param text A main body text to be displayed
     * @param label An optional short, informational String to be displayed in the
     *  [BannerCardView]'s upper left hand corner. Passing null or not providing a value will
     *  hide the label.
     * @param labelListener A [View.OnClickListener] to listen for clicks on the banner's label
     * @param topButton An optional String to set for the top button text. Passing null or not
     *  providing a value will hide the top button.
     * @param topButtonListener A [View.OnClickListener] to listen for clicks on the top button
     * @param bottomButton An optional String to set as the bottom button's text. Passing null
     *  or not providing a value will hide the bottom button.
     */
    fun setBanner(
            text: String,
            label: String? = null,
            labelListener: OnClickListener? = null,
            topButton: String? = null,
            topButtonListener: OnClickListener? = null,
            bottomButton: String? = null,
            bottomButtonListener: OnClickListener? = null
    ) {
        setBannerLabel(label, labelListener)
        setBannerText(text)
        setBannerTopButton(topButton, topButtonListener)
        setBannerBottomButton(bottomButton, bottomButtonListener)
    }

    fun setBannerLabel(label: String?, listener: OnClickListener?) {
        setBannerLabelText(label)
        setBannerLabelOnClickListener(listener)
    }

    fun setBannerLabelText(label: String?) {
        bannerLabel = label
        if (bannerLabel != null) {
            bannerLabelTextView.text = label
            bannerLabelTextView.visible()
        } else {
            bannerLabelTextView.text = ""
            bannerLabelTextView.gone()
        }
    }

    fun setBannerLabelOnClickListener(listener: OnClickListener?) {
        bannerLabelTextView.setOnClickListener(listener)
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