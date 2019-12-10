package space.narrate.waylan.settings.di

import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import space.narrate.waylan.settings.ui.developer.DeveloperSettingsViewModel
import space.narrate.waylan.settings.ui.settings.SettingsViewModel
import space.narrate.waylan.settings.billing.BillingManager
import space.narrate.waylan.settings.ui.thirdparty.ThirdPartyLibrariesViewModel

// Dependent on CoreModule
val settingsModule = module {

    viewModel { SettingsViewModel(get()) }

    viewModel { DeveloperSettingsViewModel(get()) }

    viewModel { ThirdPartyLibrariesViewModel() }

    // Billing
    single { BillingManager(androidContext(), get(), get()) }
}