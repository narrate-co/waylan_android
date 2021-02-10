package space.narrate.waylan.core.billing

/**
 * An object to hold billing configuration constants.
 *
 * @property SKU_MERRIAM_WEBSTER The Google Play sku for the Merriam-Webster plugin
 */
object BillingConfig {
    // Test skus that automatically return specific results. Used for testing
    const val TEST_SKU_PURCHASED = "android.test.purchased"
    const val TEST_SKU_CANCELED = "android.test.canceled"
    const val TEST_SKU_ITEM_UNAVAILABLE = "android.test.item_unavailable"

    // A variable to hold which test sku to use. Can be changed from DeveloperSettingsFragment
    var TEST_SKU = TEST_SKU_PURCHASED

    // Our actual Google Play sku
    const val SKU_MERRIAM_WEBSTER = "space.narrate.words.android.merriam_webster_plugin"
    const val SKU_MERRIAM_WEBSTER_THESAURUS = "space.narrate.words.android.merriam_webster_thesaurus_plugin"
    const val SKU_AMERICAN_HERITAGE = "space.narrate.words.android.american_heritage_plugin"
}