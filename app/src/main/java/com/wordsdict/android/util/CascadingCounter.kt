package com.wordsdict.android.util

import com.wordsdict.android.data.firestore.ViewCountDocument
import com.wordsdict.android.data.firestore.words.GlobalWord
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * A helper object which adds and resets view counts according to values in [ViewCountDocument].
 * Pass [plus] any document which is a subclass of [ViewCountDocument] to have [CascadingCounter]
 * increase period view counts appropriately.
 *
 * TODO move into Cloud Functions
 */
object CascadingCounter {

    private val TAG = CascadingCounter::class.java.simpleName

    fun plus(doc: ViewCountDocument, views: Int) {
        addToMinuteViewCount(doc, views)
        addToHourViewCount(doc, views)
        addToDayViewCount(doc, views)
        addToWeekViewCount(doc, views)
        addToMonthViewCount(doc, views)
        addToYearViewCount(doc, views)
        addToTotalViewCount(doc, views)
    }

    private fun addToMinuteViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.minuteViewCountStarted.minutesElapsed >= 1L) {
            doc.minuteViewCount = 0L
            doc.minuteViewCountStarted = nearestMinute
        }

        doc.minuteViewCount += views
    }

    private fun addToHourViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.hourViewCountStarted.minutesElapsed >= 60L) {
            doc.hourViewCount = 0L
            doc.hourViewCountStarted = nearestHour
        }

        doc.hourViewCount += views
    }

    private fun addToDayViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.dayViewCountStarted.daysElapsed >= 1L) {
            doc.dayViewCount = 0L
            doc.dayViewCountStarted = nearestDay
        }

        doc.dayViewCount += views
    }

    private fun addToWeekViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.weekViewCountStarted.daysElapsed >= 7L) {
            doc.weekViewCount = 0L
            doc.weekViewCountStarted = nearestWeek
        }

        doc.weekViewCount += views
    }

    private fun addToMonthViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.monthViewCountStarted.daysElapsed >= 30L) {
            doc.monthViewCount = 0L
            doc.monthViewCountStarted = nearestMonth
        }

        doc.monthViewCount += views
    }

    private fun addToYearViewCount(doc: ViewCountDocument, views: Int) {
        if (doc.yearViewCountStarted.daysElapsed >= 365L) {
            doc.yearViewCount = 0L
            doc.yearViewCountStarted = nearestYear
        }

        doc.yearViewCount += views
    }

    private fun addToTotalViewCount(doc: ViewCountDocument, views: Int) {
        doc.totalViewCount += views
    }
}

