package space.narrate.waylan.android.settings

import space.narrate.waylan.core.data.prefs.NightMode
import space.narrate.waylan.android.ui.dialog.RadioItemModel

class NightModeRadioItemModel(
    val nightMode: NightMode,
    selected: Boolean = false
) : RadioItemModel(nightMode.titleRes, nightMode.descRes, selected)