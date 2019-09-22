package space.narrate.waylan.android.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import space.narrate.waylan.core.ui.common.Diffable

object AdapterUtils {

    fun <T : Diffable<T>> diffableItemCallback(): DiffUtil.ItemCallback<T> =
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem.isSameAs(newItem)

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem.isContentSameAs(newItem)
            }

            override fun getChangePayload(oldItem: T, newItem: T): Any? {
                return oldItem.getChangePayload(newItem)
            }
        }

    fun <T> emptyDiffItemCallback(): DiffUtil.ItemCallback<T> =
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = false
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = false
        }

    fun inflate(parent: ViewGroup, layout: Int): View {
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }
}






