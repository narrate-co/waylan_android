package com.words.android.util

import android.content.Context
import android.view.LayoutInflater
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

