package space.narrate.waylan.wordnik.data.remote

class ApiExamples(
  val examples: List<ApiExample>
)

class ApiExample(
  val provider: Map<String, Int>?,
  val rating: Float?,
  val url: String?,
  val word: String?,
  val text: String?,
  val documentId: Long?,
  val exampleId: Long?,
  val title: String?,
  val author: String?
)