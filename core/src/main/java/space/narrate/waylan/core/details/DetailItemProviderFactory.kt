package space.narrate.waylan.core.details

/**
 * An interface which allows :app to construct data item providers via reflection. Modules which
 * wish to expose a detail item provider should implement a no-parameter class which implements
 * this interface and provides its DetailItemProvider implementation.
 */
interface DetailItemProviderFactory {
    fun getDetailItemProvider(): DetailItemProvider
}