package space.narrate.waylan.core.details

import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

/**
 * A ViewHolder which can be displayed by :app's DetailItemAdpater. Any module which wishes to
 * display data in a word's details screen should implement a [DetailItemModel] and its accompanying
 * [DetailItemViewHolder].
 */
abstract class DetailItemViewHolder(
    val view: View
): RecyclerView.ViewHolder(view), LifecycleOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {
      lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
      view.doOnAttach {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
      }
      view.doOnDetach {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
      }
    }

    open fun bind(item: DetailItemModel) { }

    override fun getLifecycle(): Lifecycle {
      return lifecycleRegistry
    }
}