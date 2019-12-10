package space.narrate.waylan.settings.billing

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

    // A variable to hold which test skus to use. Can be changed from DeveloperSettingsFragment
    var TEST_SKU_MERRIAM_WEBSTER = TEST_SKU_PURCHASED

    // Our actual Google Play sku
    const val SKU_MERRIAM_WEBSTER = "space.narrate.words.android.merriam_webster_plugin"
}