package space.narrate.waylan.merriamwebster.di

import org.koin.core.context.loadKoinModules
import org.koin.java.KoinJavaComponent.getKoin
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailProviderFactory
import space.narrate.waylan.merriamwebster.ui.MerriamWebsterDetailDataProvider
import space.narrate.waylan.merriamwebster.ui.MerriamWebsterDetailItemProvider

@Volatile
private var isModuleLoaded: Boolean = false

/**
 * The only class which should be instantiated using reflection from other modules when needing
 * instances from :merriamwebster. This class ensures all of :merriamwebster's module dependencies
 * are created before providing any instances.
 *
 * This class also serves as the surface of this module, showing what is exposed and what can
 * be used by other modules.
 */
class MerriamWebsterModuleProviderFactory : DetailProviderFactory {

    init {
        synchronized(isModuleLoaded) {
            if (!isModuleLoaded) {
                isModuleLoaded = true
                loadKoinModules(merriamWebsterModule)
            }
        }
    }

    override fun getDetailDataProvider(): DetailDataProvider =
        getKoin().get<MerriamWebsterDetailDataProvider>()

    override fun getDetailItemProvider(): DetailItemProvider =
        getKoin().get<MerriamWebsterDetailItemProvider>()
}