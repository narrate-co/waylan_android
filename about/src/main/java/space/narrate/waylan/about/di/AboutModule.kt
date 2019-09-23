package space.narrate.waylan.about.di

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import space.narrate.waylan.about.data.AboutRepository
import space.narrate.waylan.about.data.ThirdPartyLibraryStore
import space.narrate.waylan.about.ui.thirdparty.ThirdPartyLibrariesViewModel

val aboutModule = module {

    single { ThirdPartyLibraryStore }

    single { AboutRepository(get()) }

    viewModel { ThirdPartyLibrariesViewModel(get()) }
}

private val aboutModuleLoader by lazy { loadKoinModules(aboutModule) }

fun loadAboutModule() = aboutModuleLoader
