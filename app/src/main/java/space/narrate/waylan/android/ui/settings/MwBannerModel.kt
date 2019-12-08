package space.narrate.waylan.android.ui.settings

import android.content.Context
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.firestore.users.PluginState
import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.merriamWebsterState
import space.narrate.waylan.core.util.getStringOrNull

data class MwBannerModel(
    val textRes: Int,
    val topButtonRes: Int?,
    val topButtonAction: MwBannerAction?,
    val bottomButtonRes: Int?,
    val bottomButtonAction: MwBannerAction?,
    val labelRes: Int?,
    val daysRemaining: Long?,
    val email: String?
) {

    companion object {
        fun create(user: User?): MwBannerModel {
            return if (user?.isAnonymous == false) {
                createRegistered(user.merriamWebsterState, user.email)
            } else {
                createAnonymous(user?.merriamWebsterState ?: PluginState.None())
            }
        }

        fun getConcatenatedLabel(context: Context, label: Int?, daysRemaining: Long?): String? {
            return if (daysRemaining != null && label != null) {
                context.resources.getString(label, daysRemaining)
            } else {
                context.getStringOrNull(label)
            }
        }

        private fun createAnonymous(state: PluginState): MwBannerModel {
            val topButton = R.string.settings_header_anonymous_create_account_button
            val bottomButton = R.string.settings_header_anonymous_log_in_button

            var text: Int
            var label: Int? = null
            var daysRemaining: Long? = null

            when (state) {

                is PluginState.FreeTrial -> {
                    text = R.string.settings_header_anonymous_free_trial_body
                    if (state.isValid) {
                        label = R.string.mw_card_view_free_trial_days_remaining
                        daysRemaining = state.remainingDays
                    } else {
                        label = R.string.settings_header_free_trial_expired_label
                    }
                }
                else -> {
                    text = R.string.settings_header_anonymous_none_body
                }
            }

            return MwBannerModel(
                text,
                topButton,
                MwBannerAction.SIGN_UP,
                bottomButton,
                MwBannerAction.LOG_IN,
                label,
                daysRemaining,
                null
            )
        }

        private fun createRegistered(state: PluginState, email: String?): MwBannerModel {
            var text: Int
            var topButton: Int? = null
            var topButtonAction: MwBannerAction? = null
            var bottomButton: Int? = null
            var bottomButtonAction: MwBannerAction? = null
            var label: Int? = null
            var daysRemaining: Long? = null

            when (state) {
                is PluginState.None -> {
                    text = R.string.settings_header_registered_none_body
                    topButton = R.string.settings_header_registered_add_button
                    topButtonAction = MwBannerAction.LAUNCH_PURCHASE_FLOW
                }
                is PluginState.FreeTrial -> {
                    if (state.isValid) {
                        text = R.string.settings_header_registered_free_trial_body
                        label = R.string.mw_card_view_free_trial_days_remaining
                        daysRemaining = state.remainingDays
                        topButton = R.string.settings_header_registered_add_button
                        topButtonAction = MwBannerAction.LAUNCH_PURCHASE_FLOW
                    } else {
                        text = R.string.settings_header_registered_free_trial_expired_body
                        label = R.string.settings_header_free_trial_expired_label
                        topButton = R.string.settings_header_registered_add_button
                        topButtonAction = MwBannerAction.LAUNCH_PURCHASE_FLOW
                    }
                }
                is PluginState.Purchased -> {
                    if (state.isValid) {
                        text = R.string.settings_header_registered_subscribed_body
                        label = R.string.settings_header_added_label
                    } else {
                        text = R.string.settings_header_registered_subscribed_expired_body
                        label = R.string.settings_header_expired_label
                        topButton = R.string.settings_header_renew_top_button
                        topButtonAction = MwBannerAction.LAUNCH_PURCHASE_FLOW
                    }
                }
            }

            return MwBannerModel(
                text,
                topButton,
                topButtonAction,
                bottomButton,
                bottomButtonAction,
                label,
                daysRemaining,
                email
            )
        }
    }
}