package space.narrate.waylan.merriamwebster_thesaurus.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.card.MaterialCardView
import space.narrate.waylan.merriamwebster_thesaurus.R

class MerriamWebsterThesaurusCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        val view = View.inflate(context, R.layout.mw_thesaurus_card_layout, this)

        // TODO Get view
    }
}