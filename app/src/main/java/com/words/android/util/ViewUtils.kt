package com.words.android.util

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.words.android.R
import com.words.android.data.disk.wordset.Synonym

fun Synonym.toChip(context: Context, chipGroup: ChipGroup?, onClick: ((synonym: Synonym) -> Unit)? = null): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(R.layout.details_chip_layout, chipGroup, false) as Chip
    chip.chipText = this.synonym
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}

fun Activity.hideSoftKeyboard() {
    val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = currentFocus ?: View(this)
    im.hideSoftInputFromWindow(view.windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN)
}

