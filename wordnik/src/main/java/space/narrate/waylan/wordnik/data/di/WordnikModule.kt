package space.narrate.waylan.wordnik.data.di

import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import space.narrate.waylan.wordnik.data.WordnikStore
import space.narrate.waylan.wordnik.data.local.WordnikDatabase
import space.narrate.waylan.wordnik.data.remote.RetrofitService

val wordnikModule = module {

  single { WordnikDatabase.getInstance(androidContext()) }

  single {
    WordnikStore(
      RetrofitService.getInstance(),
      get<WordnikDatabase>().wordnikDao(),
      Dispatchers.IO
    )
  }
}