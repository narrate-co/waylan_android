package space.narrate.waylan.core.data.firestore.users

import space.narrate.waylan.core.billing.BillingConfig
import java.lang.IllegalArgumentException

/**
 * All available add-ons which can be purchased and appended to a [User] document.
 */
enum class AddOn(val id: String, val sku: String) {
    MERRIAM_WEBSTER(
        "merraimwebser",
        BillingConfig.SKU_MERRIAM_WEBSTER
    ),
    MERRIAM_WEBSTER_THESAURUS(
        "merriamwebster_thesaurus",
        BillingConfig.SKU_MERRIAM_WEBSTER_THESAURUS
    );

    companion object {
        fun fromId(id: String): AddOn {
            return when (id) {
                MERRIAM_WEBSTER.id -> MERRIAM_WEBSTER
                MERRIAM_WEBSTER_THESAURUS.id -> MERRIAM_WEBSTER_THESAURUS
                else -> throw IllegalArgumentException("The id '$id' is not a valid AddOn id.")
            }
        }
    }
}

