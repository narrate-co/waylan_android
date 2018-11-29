package com.wordsdict.android.util

import android.content.pm.ActivityInfo

fun isPortraitToLandscape(old: Int, new: Int): Boolean {
    return (old == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || old == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) &&
            (new == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || new == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
}

fun isLandscapeToPortrait(old: Int, new: Int): Boolean {
    return (old == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || old == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) &&
            (new == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || new == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT)
}

