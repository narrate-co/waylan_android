package com.wordsdict.android.util

import java.util.*

object CascadingCounter {

    fun plus(dayCounter: MutableMap<Date, Int>, weekCounter: MutableMap<Date, Int>) {
        cascadeToDayCount(dayCounter, weekCounter)
    }

    private fun cascadeToDayCount(dayCounter: MutableMap<Date, Int>, weekCounter: MutableMap<Date, Int>) {
        val today = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        var lastDayCounted: Calendar? = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        today.time = Date()

        val lastDate = dayCounter.keys.firstOrNull()
        if (lastDate == null) {
            lastDayCounted = null
        } else {
            lastDayCounted?.time = lastDate
        }


        //update day counter
        if (lastDayCounted == null) {
            //this word has never been seen!
            //start new count
            dayCounter[today.time] = 1
        } else if (today.isSameDayAs(lastDayCounted)) {
            //increment count
            val newCount = dayCounter.getValue(today.time) + 1
            dayCounter[today.time] = newCount
        } else {
            //reset count, push
            cascadeToWeekCount(dayCounter.toMap(), weekCounter)
            dayCounter.clear()
            dayCounter[today.time] = 1
        }

        //update week counter
        cascadeToWeekCount(dayCounter.toMap(), weekCounter)
    }

    private fun cascadeToWeekCount(dayCounter: Map<Date, Int>, weekCounter: MutableMap<Date, Int>) {

    }}

