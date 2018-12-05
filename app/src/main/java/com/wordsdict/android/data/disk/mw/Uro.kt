package com.wordsdict.android.data.disk.mw

/**
 * An object that represents an alternative to its parent words morpheme, usually replacing a prefix
 * or suffix.
 *
 * For example, <i>quiescent</i> will have a [Uro] for <i>quiescently</i>.
 */
data class Uro(
        val ure: String,
        val fl: String
)

