package com.wordsdict.android

import java.util.*

fun Date.addMinutes(minutes: Int): Date {
    return add(Calendar.MINUTE, minutes)
}

fun Date.addHours(hours: Int): Date {
    return add(Calendar.HOUR, hours)
}

fun Date.addDays(days: Int): Date {
    return add(Calendar.DATE, days)
}

fun Date.addMonths(months: Int): Date {
    return add(Calendar.MONTH, months)
}

fun Date.addYears(years: Int): Date {
    return add(Calendar.YEAR, years)
}

private fun Date.add(field: Int, amount: Int): Date {
     val cal = Calendar.getInstance()
    cal.time = this
    cal.add(field, amount)
    return cal.time
}



