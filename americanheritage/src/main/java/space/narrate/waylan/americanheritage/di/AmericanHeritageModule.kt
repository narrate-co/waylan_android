package space.narrate.waylan.americanheritage.di

import org.koin.dsl.module
import space.narrate.waylan.americanheritage.data.AmericanHeritageRepository
import space.narrate.waylan.americanheritage.ui.AmericanHeritageDetailDataProvider
import space.narrate.waylan.americanheritage.ui.AmericanHeritageDetailItemProvider

val americanHeritageModule = module {

  single { AmericanHeritageRepository(get()) }

  single { AmericanHeritageDetailDataProvider(get(), get()) }

  single { AmericanHeritageDetailItemProvider() }
}