package space.narrate.words.android.data.disk.mw


/**
 * A class to hold Merriam-Webster pronunciation url filenames.
 *
 * Parameters in [Sound] are typically only the filename followed by their respective file listType.
 * For example, the [MwWord] <i>quiescent</i>'s [Sound] might have the [wavs] 'quiesce_1.wavs'.
 *
 * To generate the fully qualified Merriam-Webster url from this filename, use the following:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/{first character of the [Sound.wavs] or [Sound.wprs]}/{[Sound.wavs] or [Sound.wprs]}
 *
 * For <i>quiescent</i>, the url would be:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/q/quiesce_q.wavs
 *
 * Furthermore, you can strip the [Sound.wavs] or [Sound.wprs]'s trailing file listType and replace it
 * with .mp3. The full url would then be:
 *
 * https://media.merriam-webster.com/audio/prons/en/us/mp3/q/quiesce_q.mp3
 */
data class Sound(
        val wavs: List<String>,
        val wprs: List<String>
)

