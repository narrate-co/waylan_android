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