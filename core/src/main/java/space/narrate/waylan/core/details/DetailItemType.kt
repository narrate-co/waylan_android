package space.narrate.waylan.core.details

/**
 * Enumeration tha lists all available [DetailItemModel] types.
 *
 * [order] is the order in which each item should be displayed vertically.
 */
enum class DetailItemType(val order: Int) {
    TITLE(1),
    MERRIAM_WEBSTER(2),
    MERRIAM_WEBSTER_THESAURUS(3),
    WORDSET(4),
    EXAMPLE(5)
}