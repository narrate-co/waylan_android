package com.words.android.util

interface Diffable<T> {
    fun equalTo(newOther: T): Boolean
    fun contentsSameAs(newOther: T): Boolean
    fun getChangePayload(newOther: T): Any?
}

