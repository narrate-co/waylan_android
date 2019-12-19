package space.narrate.waylan.settings.ui.addons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import space.narrate.waylan.core.data.firestore.users.AddOn
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.settings.ui.addons.AddOnItemModel.MerriamWebster
import space.narrate.waylan.settings.ui.addons.AddOnItemModel.MerriamWebsterThesaurus

/**
 * A LiveData mediator that listens to multiple live data objects and posts values
 * to a merger only when any sub-source has changed.
 */
class AddOnItemListMediatorLiveData : MediatorLiveData<List<AddOnItemModel>>() {

    private var merriamWebsterItem: MerriamWebster? = null
    private var merriamWebsterThesaurusItem: MerriamWebsterThesaurus? = null

    fun addSource(data: LiveData<UserAddOn>) {
        addSource(data) { userAddOn ->
            set(when(val addOn = AddOn.fromId(userAddOn.id)) {
                AddOn.MERRIAM_WEBSTER -> MerriamWebster(addOn, userAddOn)
                AddOn.MERRIAM_WEBSTER_THESAURUS -> MerriamWebsterThesaurus(addOn, userAddOn)
            })
        }
    }

    fun set(item: AddOnItemModel) {
        when (item) {
            is MerriamWebster -> {
                if (shouldUpdateList(merriamWebsterItem, item)) {
                    merriamWebsterItem = item
                    updateList()
                }
            }
            is MerriamWebsterThesaurus ->
                if (shouldUpdateList(merriamWebsterThesaurusItem, item)) {
                    merriamWebsterThesaurusItem = item
                    updateList()
            }
        }
    }

    private fun shouldUpdateList(oldItem: AddOnItemModel?, newItem: AddOnItemModel): Boolean {
        if (oldItem == null) return true

        return !oldItem.isSameAs(newItem) || !oldItem.isContentSameAs(newItem)
    }

    private fun updateList() {
        // post value
        postValue(listOfNotNull(
            merriamWebsterItem,
            merriamWebsterThesaurusItem
        ))
    }

}