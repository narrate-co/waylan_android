package space.narrate.waylan.merriamwebster.ui

import space.narrate.waylan.core.data.firestore.users.User
import space.narrate.waylan.core.data.firestore.users.UserAddOn
import space.narrate.waylan.core.data.firestore.users.isValid
import space.narrate.waylan.merriamwebster.data.local.MwDefinition
import space.narrate.waylan.merriamwebster.data.local.MwWord
import space.narrate.waylan.merriamwebster.data.local.MwWordAndDefinitionGroups

/**
 * An object which handles generating a list of [MerriamWebsterItemModel] which should be displayed
 * according to a given [MwWordAndDefinitionGroups] list and [User].
 */
object MerriamWebsterList {

    fun generate(
        entries: List<MwWordAndDefinitionGroups>,
        userAddOn: UserAddOn?
    ): List<MerriamWebsterItemModel> {
        val list = mutableListOf<MerriamWebsterItemModel>()

        // Exit early if user is not valid, showing the permission pane.
        if (userAddOn?.isValid == false) {
            list.add(MerriamWebsterItemModel.PermissionPaneModel())
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
            list.add(MerriamWebsterItemModel.RelatedModel(
                allRelatedWords.hashCode().toString(),
                allRelatedWords
            ))
        }

        return list
    }

    private fun generatePartOfSpeech(word: MwWord): MerriamWebsterItemModel.PartOfSpeechModel {
        val sb = StringBuilder()
        sb.append(word.partOfSpeech)
        sb.append("  |  ${word.phonetic.replace("*", " • ")}")
        return MerriamWebsterItemModel.PartOfSpeechModel(word.id, sb.toString())
    }

    private fun generateDefinition(
        definition: MwDefinition
    ): MerriamWebsterItemModel.DefinitionModel {
        return MerriamWebsterItemModel.DefinitionModel(definition.def, definition.def)
    }
}