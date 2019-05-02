package space.narrate.words.android.ui.details

import space.narrate.words.android.data.disk.mw.OrderedDefinitionItem
import space.narrate.words.android.data.disk.mw.Word
import space.narrate.words.android.data.disk.mw.WordAndDefinitions
import space.narrate.words.android.data.firestore.users.User
import space.narrate.words.android.data.firestore.users.merriamWebsterState

/**
 * An object which handles generating a list of [MerriamWebsterListItem] which should be displayed
 * according to a given [WordAndDefinitions] list and [User].
 */
object MerriamWebsterList {

    fun generate(entries: List<WordAndDefinitions>, user: User?): List<MerriamWebsterListItem> {
        val list = mutableListOf<MerriamWebsterListItem>()

        // Exit early if user is not valid, showing the permission pane.
        if (user?.merriamWebsterState?.isValid == false) {
            list.add(MerriamWebsterListItem.PermissionPane())
            return list
        }

        val suggestions = mutableListOf<String>()
        val related = mutableListOf<String>()

        entries.forEach { entry ->
            if (entry.word != null && !entry.definitions.isNullOrEmpty()) {
                // Add part of speech
                list.add(generatePartOfSpeech(entry.word!!))

                // Add definitions
                entry.definitions.flatMap { it.definitions }.forEach { definition ->
                    list.add(generateDefinition(definition))
                }
            }

            // Collect related and suggested words
            suggestions.addAll(entry.word!!.suggestions.filterNot { it == entry.word!!.word })
            related.addAll(entry.word!!.relatedWords.filterNot { it == entry.word!!.word })
        }

        // Add suggestions and related words
        val allRelatedWords = (suggestions + related).distinct()
        if (allRelatedWords.isNotEmpty()) {
            list.add(MerriamWebsterListItem.Related(
                    allRelatedWords.hashCode().toString(),
                    allRelatedWords
            ))
        }

        return list
    }

    private fun generatePartOfSpeech(word: Word): MerriamWebsterListItem.PartOfSpeech {
        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        return MerriamWebsterListItem.PartOfSpeech(word.id, sb.toString())
    }

    private fun generateDefinition(
            definition: OrderedDefinitionItem
    ): MerriamWebsterListItem.Definition {
        return MerriamWebsterListItem.Definition(definition.def, definition.def)
    }
}