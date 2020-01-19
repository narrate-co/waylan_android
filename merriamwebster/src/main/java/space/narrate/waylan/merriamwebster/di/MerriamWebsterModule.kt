package space.narrate.waylan.merriamwebster.di

import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import space.narrate.waylan.merriamwebster.data.MerriamWebsterRepository
import space.narrate.waylan.merriamwebster.data.local.MerriamWebsterDatabase
import space.narrate.waylan.merriamwebster.data.MerriamWebsterStore
import space.narrate.waylan.merriamwebster.data.remote.RetrofitService
import space.narrate.waylan.merriamwebster.ui.MerriamWebsterDetailDataProvider
import space.narrate.waylan.merriamwebster.ui.MerriamWebsterDetailItemProvider

val merriamWebsterModule = module {

    single {
        MerriamWebsterDatabase.getInstance(androidContext())
    }

    single {
        MerriamWebsterStore(
            RetrofitService.getInstance(),
            get<MerriamWebsterDatabase>().mwDao(),
            Dispatchers.IO
        )
    }

    single { MerriamWebsterRepository(get()) }

    single { MerriamWebsterDetailDataProvider(get(), get()) }

    single { MerriamWebsterDetailItemProvider() }
}
