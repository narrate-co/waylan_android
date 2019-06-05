package space.narrate.waylan.android.ui.settings

import space.narrate.waylan.android.data.prefs.Orientation
import space.narrate.waylan.android.ui.dialog.RadioItemModel

class OrientationRadioItemModel(
    val orientation: Orientation,
    selected: Boolean = false
) : RadioItemModel(orientation.title, orientation.desc, selected)
