package com.wordsdict.android.ui.details

interface Diffable<T> {
    /**
     * Are [T] and [newOther] of the same type
     *
     * @return true if [T] and [newOther] are of the same type
     */
    fun equalTo(newOther: T): Boolean

    /**
     * Are all relevant values in [T] the same as [newOther]
     *
     * @return true if there is nothing new in [newOther] that should have us to consider [newOther]
     *  as updated
     */
    fun contentsSameAs(newOther: T): Boolean

    /**
     * Get an Object which represents the values which have changed from [T] to [newOther]. The
     * returned object is then used in [RecyclerView] to set only changed UI elements.
     *
     * @return an Object representing what has changed from [T] to [newOther]
     */
    fun getChangePayload(newOther: T): Any?
}

