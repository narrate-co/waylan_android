package space.narrate.waylan.merriamwebster_thesaurus.di

import org.koin.core.context.loadKoinModules
import org.koin.java.KoinJavaComponent.getKoin
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailProviderFactory
import space.narrate.waylan.merriamwebster_thesaurus.ui.MerriamWebsterThesaurusDetailDataProvider
import space.narrate.waylan.merriamwebster_thesaurus.ui.MerriamWebsterThesaurusDetailItemProvider

@Volatile
private var isModuleLoaded: Boolean = false

/**
 * The only class which should be instantiated using reflection from other modules when needing
 * instances from :merriamwebster_thesaurus. This class ensures all of :merriamwebster_thesaursu's
 * module dependencies are created before providing any instances.
 *
 * This class also serves as the surface of this module, showing what is exposed and what can
 * be used by other modules.
 */
class MerriamWebsterThesaurusModuleProviderFactory : DetailProviderFactory {

    init {
        synchronized(isModuleLoaded) {
            if (!isModuleLoaded) {
                isModuleLoaded = true
                loadKoinModules(merriamWebsterThesaurusModule)
            }
        }
    }

    override fun getDetailDataProvider(): DetailDataProvider =
        getKoin().get<MerriamWebsterThesaurusDetailDataProvider>()

    override fun getDetailItemProvider(): DetailItemProvider =
        getKoin().get<MerriamWebsterThesaurusDetailItemProvider>()
}