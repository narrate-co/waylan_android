package space.narrate.words.android.ui.settings

import space.narrate.words.android.data.prefs.NightMode
import space.narrate.words.android.ui.dialog.RadioItemModel

class NightModeRadioItemModel(
    val nightMode: NightMode,
    selected: Boolean = false
) : RadioItemModel(nightMode.titleRes, nightMode.descRes, selected)