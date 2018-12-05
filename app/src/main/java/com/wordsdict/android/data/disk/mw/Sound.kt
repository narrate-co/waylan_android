package com.wordsdict.android.data.disk.mw


/**
 * A class to hold Merriam-Webster pronunciation url filenames.
 *
 * Parameters in [Sound] are typically only the filename followed by their respective file type.
 * For example, the [Word] <i>quiescent</i>'s [Sound] might have the [wav] 'quiesce_1.wav'.
 *
 * To generate the fully qualified Merriam-Webster url from this filename, use the following:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/{first character of the [Sound.wav] or [Sound.wpr]}/{[Sound.wav] or [Sound.wpr]}
 *
 * For <i>quiescent</i>, the url would be:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/q/quiesce_q.wav
 *
 * Furthermore, you can strip the [Sound.wav] or [Sound.wpr]'s trailing file type and replace it
 * with .mp3. The full url would then be:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/q/quiesce_q.mp3
 */
data class Sound(
        val wav: String,
        val wpr: String
)

