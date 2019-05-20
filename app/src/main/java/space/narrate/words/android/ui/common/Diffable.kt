package space.narrate.words.android.ui.common

interface Diffable<T> {
    /**
     * Are [T] and [newOther] of the same listType
     *
     * @return true if [T] and [newOther] are of the same listType
     */
    fun isSameAs(newOther: T): Boolean

    /**
     * Are all relevant values in [T] the same as [newOther]
     *
     * @return true if there is nothing new in [newOther] that should have us to consider [newOther]
     *  as updated
     */
    fun isContentSameAs(newOther: T): Boolean

    /**
     * Get an Object which represents the values which have changed from [T] to [newOther]. The
     * returned object is then used in [RecyclerView] to set only changed UI elements.
     *
     * @return an Object representing what has changed from [T] to [newOther]
     */
    fun getChangePayload(newOther: T): Any? = null
}

