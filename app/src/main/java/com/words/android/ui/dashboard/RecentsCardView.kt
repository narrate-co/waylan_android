package com.words.android.ui.dashboard

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.card.MaterialCardView
import com.words.android.R

class RecentsCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.materialCardViewStyle
): MaterialCardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.dashboard_recents_card_layout, this)
    }
}


