package space.narrate.waylan.about.data

class AboutRepository(
    thirdPartyLibraryStore: ThirdPartyLibraryStore
) {
    val allThirdPartyLibraries: List<ThirdPartyLibrary> = thirdPartyLibraryStore.all
}