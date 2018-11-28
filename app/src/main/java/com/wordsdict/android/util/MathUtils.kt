package com.wordsdict.android.util

fun constrained(value: Float, min: Float, max: Float): Float {
    return Math.min(Math.max(value, min), max)
}

