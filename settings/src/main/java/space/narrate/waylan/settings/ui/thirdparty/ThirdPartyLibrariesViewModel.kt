package space.narrate.waylan.settings.ui.thirdparty

import androidx.lifecycle.ViewModel
import space.narrate.waylan.settings.data.ThirdPartyLibrary
import space.narrate.waylan.settings.data.ThirdPartyLibraryStore

class ThirdPartyLibrariesViewModel : ViewModel() {

    val thirdPartyLibraries: List<ThirdPartyLibrary>
        get() = ThirdPartyLibraryStore.all
}