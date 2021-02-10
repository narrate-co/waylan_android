package space.narrate.waylan.core.details

import androidx.lifecycle.LiveData

/**
 * An interface which subclasses can implement to allow their data to be loaded as part
 * of the details screen. DetailDataProviders should be added to :app's DetailDataProviderRegistry.
 * When a new word is asked to be defined, the details screen will query all registered
 * data providers by calling their [loadWord] method.
 */
interface DetailDataProvider {
    fun loadWord(word: String): LiveData<DetailItemModel>
}