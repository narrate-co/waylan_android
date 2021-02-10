package space.narrate.waylan.merriamwebster_thesaurus.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import space.narrate.waylan.merriamwebster_thesaurus.data.MerriamWebsterThesaurusRepository
import space.narrate.waylan.merriamwebster_thesaurus.data.local.ThesaurusDatabase
import space.narrate.waylan.merriamwebster_thesaurus.data.MerriamWebsterThesaurusStore
import space.narrate.waylan.merriamwebster_thesaurus.data.remote.RetrofitService
import space.narrate.waylan.merriamwebster_thesaurus.ui.MerriamWebsterThesaurusDetailDataProvider
import space.narrate.waylan.merriamwebster_thesaurus.ui.MerriamWebsterThesaurusDetailItemProvider

/**
 * All module dependencies for :merriamwebseter_thesaurus.
 */
val merriamWebsterThesaurusModule = module {

    single { ThesaurusDatabase.getInstance(androidContext()) }

    single {
        MerriamWebsterThesaurusStore(
            RetrofitService.getInstance(),
            get<ThesaurusDatabase>().thesaurusDao()
        )
    }

    single {
        MerriamWebsterThesaurusRepository(get())
    }

    single { MerriamWebsterThesaurusDetailDataProvider(get(), get()) }

    single { MerriamWebsterThesaurusDetailItemProvider() }
}