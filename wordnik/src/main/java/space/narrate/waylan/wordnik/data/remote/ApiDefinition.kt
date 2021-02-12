package space.narrate.waylan.wordnik.data.remote

/**
 * An object representing the parsed json returned from Wordnik's definition
 * endpoint.
 */
class ApiDefinition(
  val id: String?,
  val partOfSpeech: String?,
  val attributionText: String?,
  val sourceDictionary: String?,
  val text: String?,
  val sequence: String?,
  val score: Int?,
  val labels: List<Map<String ,String>>?,
  val citations: List<Map<String, String>>?,
  val word: String?,
  val relatedWords: List<Map<String, String>>?,
  val exampleUses: List<Map<String, String>>?,
  val textProns: List<Map<String, String>>?,
  val notes: List<Map<String, String>>?,
  val attributionUrl: String?,
  val wordnikUrl: String?
)
