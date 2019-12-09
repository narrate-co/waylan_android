package space.narrate.waylan.android.util

import android.content.Context
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import space.narrate.waylan.core.data.wordset.Synonym
import space.narrate.waylan.core.util.toChip

fun Synonym.toChip(
    context: Context,
    chipGroup: ChipGroup?,
    onClick: ((synonym: Synonym) -> Unit)? = null
): Chip {
   return synonym.toChip(context, chipGroup) {
        if (onClick != null && it == synonym) onClick(this)
    }
}
