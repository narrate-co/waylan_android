package space.narrate.waylan.settings

import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.settings.dialog.RadioItemModel

class NightModeRadioItemModel(
    val nightMode: NightMode,
    selected: Boolean = false
) : RadioItemModel(nightMode.titleRes, nightMode.descRes, selected)