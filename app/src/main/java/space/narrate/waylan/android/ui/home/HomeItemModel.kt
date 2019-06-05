package space.narrate.waylan.android.ui.home

import space.narrate.waylan.android.ui.common.Diffable
import space.narrate.waylan.android.ui.list.ListType

sealed class HomeItemModel : Diffable<HomeItemModel> {

    data class ItemModel(
        val listType: ListType,
        val titleRes: Int,
        val preview: String = ""
    ): HomeItemModel(), Diffable<HomeItemModel> {

        override fun isSameAs(newOther: HomeItemModel): Boolean {
            return newOther is ItemModel
        }

        override fun isContentSameAs(newOther: HomeItemModel): Boolean {
            if (newOther !is ItemModel) return false
            return titleRes == newOther.titleRes &&
                preview == newOther.preview &&
                listType == newOther.listType
        }
    }

    object DividerModel : HomeItemModel() {
        override fun isSameAs(newOther: HomeItemModel): Boolean {
            return newOther is DividerModel
        }

        override fun isContentSameAs(newOther: HomeItemModel): Boolean {
            return true
        }
    }

    object SettingsModel : HomeItemModel() {
        override fun isSameAs(newOther: HomeItemModel): Boolean {
            return newOther is SettingsModel
        }

        override fun isContentSameAs(newOther: HomeItemModel): Boolean {
            return true
        }
    }

}
