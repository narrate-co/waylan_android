package space.narrate.waylan.about.ui.thirdparty

import androidx.lifecycle.ViewModel
import space.narrate.waylan.about.data.AboutRepository
import space.narrate.waylan.about.data.ThirdPartyLibrary

class ThirdPartyLibrariesViewModel(
    private val aboutRepository: AboutRepository
) : ViewModel() {

    val thirdPartyLibraries: List<ThirdPartyLibrary>
        get() = aboutRepository.allThirdPartyLibraries
}