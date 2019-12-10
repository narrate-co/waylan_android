package space.narrate.waylan.settings.ui.settings

import space.narrate.waylan.core.data.prefs.Orientation
import space.narrate.waylan.settings.dialog.RadioItemModel

class OrientationRadioItemModel(
    val orientation: Orientation,
    selected: Boolean = false
) : RadioItemModel(orientation.title, orientation.desc, selected)
