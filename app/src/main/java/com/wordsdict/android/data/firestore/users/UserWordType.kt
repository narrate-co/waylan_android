package com.wordsdict.android.data.firestore.users

/**
 * An enumeration of flags which can be set on a [UserWord] by being included in [UserWord.types]
 *
 * @property FAVORITED Whether the user has favorited the word
 * @property RECENT Whether the user has recently viewed the word
 * @property EDITED (Currently unused) Whether the user has edited the word/definitions
 *  themselves
 */
enum class UserWordType {
    FAVORITED, RECENT, EDITED
}

