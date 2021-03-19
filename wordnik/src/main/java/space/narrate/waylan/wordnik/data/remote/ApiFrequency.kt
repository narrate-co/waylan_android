package space.narrate.waylan.wordnik.data.remote

class ApiFrequency(
  val frequency: List<ApiFrequencyYear>?,
  val totalCount: Int?,
  val word: String?,
  val unknownYearCount: Int?
)

class ApiFrequencyYear(
  val year: String?,
  val count: Int?
)