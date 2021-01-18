package space.narrate.waylan.core.details

/**
 * Enumeration of all available [DetailItemModel] types.
 *
 * [order] is the order in which each item should be displayed vertically.
 */
enum class DetailItemType(val order: Int) {
    MERRIAM_WEBSTER(2),
    MERRIAM_WEBSTER_THESAURUS(3),
    WORDSET(4),
    DEFINITION(5),
    EXAMPLE(6)
}