package space.narrate.waylan.merriamwebster.util

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import space.narrate.waylan.merriamwebster.R

fun String.toRelatedChip(
    context: Context,
    chipGroup: ChipGroup?,
    onClick: ((word: String) -> Unit)? = null
): Chip {
    val chip: Chip = LayoutInflater.from(context).inflate(
        R.layout.merriam_webster_related_chip_layout,
        chipGroup,
        false
    ) as Chip
    val underlinedString = SpannableString(this)
    underlinedString.setSpan(UnderlineSpan(),0,this.length,0)
    chip.text = underlinedString
    chip.setOnClickListener {
        if (onClick != null) onClick(this)
    }
    return chip
}