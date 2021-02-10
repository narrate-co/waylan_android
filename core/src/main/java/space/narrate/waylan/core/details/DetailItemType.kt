package space.narrate.waylan.core.details

/**
 * Enumeration of all available [DetailItemModel] types.
 *
 * [order] is the order in which each item should be displayed vertically.
 */
enum class DetailItemType(val order: Int) {
    /** Entry from Merriam-Webster. */
    MERRIAM_WEBSTER(2),
    /** Entry from Merriam-Webster Thesaurus. */
    MERRIAM_WEBSTER_THESAURUS(3),
    /** Entry for American-Heritage. */
    AMERICAN_HERITAGE(4),
    /** Entry from Wordset. */
    WORDSET(5),
    /** Definitions that are created by users. */
    DEFINITION(6),
    /** Examples that are created by users. */
    EXAMPLE(7)
}