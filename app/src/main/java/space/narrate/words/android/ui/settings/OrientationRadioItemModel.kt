package space.narrate.words.android.ui.settings

import space.narrate.words.android.data.prefs.Orientation
import space.narrate.words.android.ui.dialog.RadioItemModel

class OrientationRadioItemModel(
    val orientation: Orientation,
    selected: Boolean = false
) : RadioItemModel(orientation.title, orientation.desc, selected)
