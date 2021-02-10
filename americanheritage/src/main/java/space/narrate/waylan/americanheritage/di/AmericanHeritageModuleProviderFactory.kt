package space.narrate.waylan.americanheritage.di

import org.koin.core.context.loadKoinModules
import org.koin.java.KoinJavaComponent.getKoin
import space.narrate.waylan.americanheritage.ui.AmericanHeritageDetailDataProvider
import space.narrate.waylan.americanheritage.ui.AmericanHeritageDetailItemProvider
import space.narrate.waylan.core.details.DetailDataProvider
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailProviderFactory

@Volatile
private var isModuleLoaded: Boolean = false

class AmericanHeritageModuleProviderFactory : DetailProviderFactory {

  init {
    synchronized(isModuleLoaded) {
      if (!isModuleLoaded) {
        isModuleLoaded = true
        loadKoinModules(americanHeritageModule)
      }
    }
  }

  override fun getDetailDataProvider(): DetailDataProvider =
    getKoin().get<AmericanHeritageDetailDataProvider>()

  override fun getDetailItemProvider(): DetailItemProvider =
    getKoin().get<AmericanHeritageDetailItemProvider>()
}