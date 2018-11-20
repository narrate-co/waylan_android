package com.wordsdict.android.billing

object BillingConfig {
    const val USE_TEST_SKUS = true

    const val TEST_SKU_PURCHASED = "android.test.purchased"
    const val TEST_SKU_CANCELED = "android.test.canceled"
    const val TEST_SKU_ITEM_UNAVAILABLE = "android.test.item_unavailable"

    var TEST_SKU_MERRIAM_WEBSTER = TEST_SKU_PURCHASED

    const val SKU_MERRIAM_WEBSTER = "com.wordsdict.android.merriam_webster_plugin"
}