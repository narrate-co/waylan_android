package space.narrate.waylan.android.settings

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import space.narrate.waylan.core.data.repo.FirestoreTestData
import space.narrate.waylan.android.R

class MwBannerModelTest {

    @Test
    fun nonAnonymousFreeUser_shouldDisplayOptionsToAddPlugin() {
        val banner = MwBannerModel.create(FirestoreTestData.registeredFreeValidUser)

        assertThat(banner.topButtonAction).isEqualTo(MwBannerAction.LAUNCH_PURCHASE_FLOW)
    }

    @Test
    fun nonAnonymousPurchasedUser_shouldDisplayAddedBanner() {
        val banner = MwBannerModel.create(FirestoreTestData.registeredPurchasedValidUser)

        assertThat(banner.topButtonAction).isNull()
        assertThat(banner.labelRes).isEqualTo(R.string.settings_header_added_label)
        assertThat(banner.textRes).isEqualTo(R.string.settings_header_registered_subscribed_body)
    }
}