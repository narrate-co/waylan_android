package space.narrate.waylan.android.ui.home

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject

class HomeItemModelListSubject(
    failureMetadata: FailureMetadata,
    list: List<HomeItemModel>
) : Subject<HomeItemModelListSubject, List<HomeItemModel>>(failureMetadata, list) {

    fun orderedContentsAreSameAs(expected: List<HomeItemModel>) {
        expected.forEachIndexed { i, itemModel ->
            if (actual().lastIndex < i) {
                failWithActual(Fact.simpleFact(
                    "Expected to have ${expected.size} items but only contains ${actual().size} items"
                ))
            }
            if (!itemModel.isSameAs(actual()[i])) {
                failWithActual(Fact.simpleFact(
                    "Expected item $i to be ${itemModel::class.java.simpleName} but found ${actual()[i]::class.java.simpleName}"
                ))
            }
            if (!itemModel.isContentSameAs(actual()[i])) {
                failWithActual(Fact.simpleFact(
                    "Expected item $i to have contents of $itemModel but was ${actual()[i]}"
                ))
            }
        }
    }
}

fun homeItemModelList(): Subject.Factory<HomeItemModelListSubject, List<HomeItemModel>> {
    return Subject.Factory<HomeItemModelListSubject, List<HomeItemModel>> { metaData, target -> HomeItemModelListSubject(metaData, target) }
}
