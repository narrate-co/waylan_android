package space.narrate.words.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Helper method for waiting to get a single value form a live data object.
 */
val <T> LiveData<T>.valueBlocking: T
    get() = valueBlocking(1)

/**
 * Wait for the receiving live data object to receive a specified number of
 * calls to [Observer.onChanged] before unblocking the thread and returning.
 */
fun <T> LiveData<T>.valueBlocking(afterChangedCount: Int = 1): T {
    val data = arrayOfNulls<Any>(1)
    val latch = CountDownLatch(afterChangedCount)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data[0] = o
            latch.countDown()
            if (latch.count == 0L) {
                removeObserver(this)
            }
        }
    }
    observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)

    @Suppress("UNCHECKED_CAST")
    return data[0] as T
}