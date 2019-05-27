package space.narrate.words.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.use
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import space.narrate.words.android.R
import space.narrate.words.android.util.getStringOrNull

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

    interface Listener {
        /**
         * The entire header view has been clicked
         */
        fun onBannerClicked()

        /**
         * The banner's textRes labelRes has been clicked
         */
        fun onBannerLabelClicked()

        /**
         * The banner's top button has been clicked
         */
        fun onBannerTopButtonClicked()

        /**
         * The banner's bottom button has been clicked
         */
        fun onBannerBottomButtonClicked()
    }

    private var labelTextView: Chip
    private var bodyTextView: AppCompatTextView
    private var topButton: MaterialButton
    private var bottomButton: MaterialButton

    private var listener: Listener? = null
    private var onTopButtonClicked: (() -> Unit)? = null
    private var onBottomButtonClicked: (() -> Unit)? = null

    init {
        val view = View.inflate(context, R.layout.banner_card_veiw_layout, this)
        labelTextView = view.findViewById(R.id.label_text_view)
        bodyTextView = view.findViewById(R.id.body_text_view)
        topButton = view.findViewById(R.id.top_button)
        bottomButton = view.findViewById(R.id.bottom_button)

        context.obtainStyledAttributes(attrs, R.styleable.BannerCardView, defStyleAttr, 0).use {
            setLabel(it.getString(R.styleable.BannerCardView_bannerLabel))
            setText(it.getString(R.styleable.BannerCardView_bannerText) ?: "")
            setTopButton(it.getString(R.styleable.BannerCardView_bannerTopButtonText))
            setBottomButton(it.getString(R.styleable.BannerCardView_bannerBottomButtonText))
        }

        setOnClickListener { listener?.onBannerClicked() }
        labelTextView.setOnClickListener { listener?.onBannerLabelClicked() }
        topButton.setOnClickListener {
            listener?.onBannerTopButtonClicked()
            onTopButtonClicked?.let { it() }
        }
        bottomButton.setOnClickListener {
            listener?.onBannerBottomButtonClicked()
            onBottomButtonClicked?.let { it() }
        }
    }


    fun setBanner(
        textRes: Int,
        labelRes: Int? = null,
        topButtonTextRes: Int? = null,
        bottomButtonTextRes: Int? = null
    ) {
        setBanner(
            context.getString(textRes),
            context.getStringOrNull(labelRes),
            context.getStringOrNull(topButtonTextRes),
            context.getStringOrNull(bottomButtonTextRes)
        )
    }

    fun setBanner(
        text: String,
        label: String? = null,topButton: String? = null,
        bottomButton: String? = null
    ) {
        setLabel(label)
        setText(text)
        setTopButton(topButton)
        setBottomButton(bottomButton)
    }

    fun setLisenter(listener: Listener): BannerCardView {
        this.listener = listener
        return this
    }

    fun setLabel(labelRes: Int?): BannerCardView {
        return setLabel(context.getStringOrNull(labelRes))
    }

    fun setLabel(label: String?): BannerCardView {
        labelTextView.text = label ?: ""
        labelTextView.visibility = if (label == null) View.GONE else View.VISIBLE
        return this
    }

    fun setText(textRes: Int): BannerCardView {
        return setText(resources.getString(textRes))
    }

    fun setText(text: String): BannerCardView {
        bodyTextView.text = text
        return this
    }

    fun setTopButton(textRes: Int?): BannerCardView {
        return setTopButton(context.getStringOrNull(textRes))
    }

    fun setTopButton(text: String?): BannerCardView {
        topButton.text = text ?: ""
        topButton.visibility = if (text == null) View.GONE else View.VISIBLE
        return this
    }

    fun setOnTopButtonClicked(onClick: (() -> Unit)?): BannerCardView {
        onTopButtonClicked = onClick
        return this
    }

    fun setBottomButton(textRes: Int?): BannerCardView {
        return setBottomButton(context.getStringOrNull(textRes))
    }

    fun setBottomButton(text: String?): BannerCardView {
        bottomButton.text = text ?: ""
        bottomButton.visibility = if (text == null) View.GONE else View.VISIBLE
        return this
    }

    fun setOnBottomButtonClicked(onClick: (() -> Unit)?): BannerCardView {
        onBottomButtonClicked = onClick
        return this
    }
}