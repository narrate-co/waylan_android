package space.narrate.waylan.about.data

class AboutRepository(
    private val thirdPartyLibraryStore: ThirdPartyLibraryStore
) {
    val allThirdPartyLibraries: List<ThirdPartyLibrary> = thirdPartyLibraryStore.all
}