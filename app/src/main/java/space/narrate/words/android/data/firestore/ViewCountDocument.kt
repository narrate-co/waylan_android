package space.narrate.words.android.data.firestore

import java.util.*

/**
 * A base document class that has fields to keep track of a document's view counts by
 * period. These fields will be maintained by Cloud Functions, updated whenever a document
 * is retrieved.
 *
 * The structure of this base document is optimized for Cloud Firestore, storing as few fields
 * as possible while allowing for collections of [ViewCountDocument] subclass documents to
 * be queried by ASC or DSC view counts + periods. Alternatives like storing each view using
 * a [Date] would result in huge sub-collections. If such granularity is needed, it would likely
 * be easier to add a Cloud function to store individual views and dates in Big Query or some
 * alternative big data table.
 */
open class ViewCountDocument(
    var totalViewCount: Long = 0L,
    var yearViewCount: Long = 0L,
    var yearViewCountStarted: Date = Date(),
    var monthViewCount: Long = 0L,
    var monthViewCountStarted: Date = Date(),
    var weekViewCount: Long = 0L,
    var weekViewCountStarted: Date = Date(),
    var dayViewCount: Long = 0L,
    var dayViewCountStarted: Date = Date(),
    var hourViewCount: Long = 0L,
    var hourViewCountStarted: Date = Date(),
    var minuteViewCount: Long = 0L,
    var minuteViewCountStarted: Date = Date()
)