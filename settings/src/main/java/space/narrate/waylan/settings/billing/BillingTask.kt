package space.narrate.waylan.settings.billing

import com.android.billingclient.api.BillingClient
import java.util.*

data class BillingTask(
    private val task: (BillingClient) -> Unit,
    val id: String = UUID.randomUUID().toString()
): Comparable<BillingTask> {

    override fun compareTo(other: BillingTask): Int = id.compareTo(other.id)

    fun run(client: BillingClient) {
        task(client)
    }
}