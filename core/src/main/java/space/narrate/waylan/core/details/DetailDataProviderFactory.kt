package space.narrate.waylan.core.details

/**
 * An interface which allows :app to construct data providers via reflection. Modules which wish
 * to expose a data provider should implement a no-parameter class which implements this interface
 * and provide its DetailDataProvider implementation.
 */
interface DetailDataProviderFactory {
    fun getDetailDataProvider() : DetailDataProvider
}