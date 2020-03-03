package space.narrate.waylan.core.data.firestore.users

val UserWord.isFavorited: Boolean
    get() = types.containsKey(UserWordType.FAVORITED.name)

val UserWord.isRecent: Boolean
    get() = types.containsKey(UserWordType.RECENT.name)