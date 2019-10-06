package space.narrate.waylan.core.details

/**
 * An interface which allows :app to construct a Factory from a module via reflection and get
 * instances of providers needed to show that module's data.
 */
interface DetailProviderFactory {
    fun getDetailDataProvider() : DetailDataProvider
    fun getDetailItemProvider(): DetailItemProvider
}