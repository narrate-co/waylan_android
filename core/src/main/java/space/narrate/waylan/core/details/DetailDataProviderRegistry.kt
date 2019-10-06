package space.narrate.waylan.core.details

/**
 * A registry with which modules can register their DetailDataProvider implementations to have
 * the details screen load their data and display it with [DetailItemProvider].
 *
 * TODO: Make providers Live to trigger a re-comp of the details screen on add/remove.
 * TODO: Ensure all data providers have a corresponding item provider?
 */
class DetailDataProviderRegistry {

    val providers: MutableSet<DetailDataProvider> = mutableSetOf()

    fun addProviders(vararg providers: DetailDataProvider) {
        this.providers.addAll(providers)
    }
}