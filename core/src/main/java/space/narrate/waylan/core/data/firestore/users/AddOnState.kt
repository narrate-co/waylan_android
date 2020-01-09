package space.narrate.waylan.core.data.firestore.users

import space.narrate.waylan.core.data.firestore.users.AddOnAction.*

enum class AddOnState {
    NONE {
        override val actions: List<AddOnAction> = listOf(TRY_FOR_FREE, ADD)
    },
    FREE_TRIAL_VALID {
        override val actions: List<AddOnAction> = listOf(ADD)
    },
    FREE_TRIAL_EXPIRED {
        override val actions: List<AddOnAction> = listOf(ADD)
    },
    PURCHASED_VALID {
        override val actions: List<AddOnAction> = emptyList()
    },
    PURCHASED_EXPIRED {
        override val actions: List<AddOnAction> = listOf(RENEW)
    };

    abstract val actions: List<AddOnAction>
}