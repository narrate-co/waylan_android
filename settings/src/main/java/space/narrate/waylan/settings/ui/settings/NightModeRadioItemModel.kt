package space.narrate.waylan.settings.ui.settings

import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.settings.ui.dialog.RadioItemModel

class NightModeRadioItemModel(
    val nightMode: NightMode,
    selected: Boolean = false
) : RadioItemModel(nightMode.titleRes, nightMode.descRes, selected)