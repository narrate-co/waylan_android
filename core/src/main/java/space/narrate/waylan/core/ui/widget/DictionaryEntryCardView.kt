package space.narrate.waylan.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import space.narrate.waylan.core.R
import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.databinding.DictionaryEntryCardLayoutBinding
import space.narrate.waylan.core.util.getFloat
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.themeFloat
import space.narrate.waylan.core.util.toChip
import space.narrate.waylan.core.util.visible

/**
 * A composite card view that is able to show an entry from any dictionary source, including
 * phonetic pronunciation, audio pronunciation, definitions, examples and related words.
 */
class DictionaryEntryCardView @JvmOverloads constructor (
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
): MaterialCardView(context, attrs, defStyleAttr) {

  interface DictionaryEntryListener {
    fun onRelatedWordClicked(word: String)
  }

  private val binding: DictionaryEntryCardLayoutBinding =
    DictionaryEntryCardLayoutBinding.inflate(LayoutInflater.from(context), this)
  private var listener: DictionaryEntryListener? = null

  init {
    background.alpha = (context.getFloat(R.dimen.translucence_01) * 255F).toInt()
    elevation = 0F
  }

  fun setListener(listener: DictionaryEntryListener?) {
    this.listener = listener
  }

  fun setStatusLabel(label: String?) {
    if (label == null || label.isEmpty()) {
      binding.textLabel.gone()
      return
    }

    binding.textLabel.text = label
    binding.textLabel.visible()
  }

  fun setDicitionaryName(name: String) {
    binding.dictionaryTitle.text = name
  }

  fun setDefinitions(entries: Map<String, List<String>>) {
    binding.run {
      definitionsListContainer.removeAllViews()
      entries.forEach { map ->
        // Create and add the overline part of speech text view
        definitionsListContainer.addView(
          createPartOfSpeechView(map.key)
        )
        // Loop through the entries for this part of speach and add each to the container
        map.value.forEach {
          definitionsListContainer.addView(
            createDefinitionView(it)
          )
        }
      }
    }
  }

  fun setExamples(examples: List<String>) {
    examples.filter { it.isNotEmpty() }
    binding.examplesListContainer.run {
      removeAllViews()
      if (examples.isEmpty()) {
        gone()
        return
      }
      visible()
      addView(createPartOfSpeechView("examples"))
      examples.forEach {
        addView(createExampleView(it))
      }
    }
  }

  fun setRelatedWords(words: List<String>) {
    binding.run {
      relatedWordsChipGroup.removeAllViews()
      if (words.isEmpty()) {
        relatedWordsScrollView.gone()
        return
      }
      relatedWordsScrollView.visible()
      words.forEach { word ->
        relatedWordsChipGroup.addView(
          word.toChip(context, relatedWordsChipGroup) {
            listener?.onRelatedWordClicked(it)
          }
        )
      }
    }
  }

  private fun createPartOfSpeechView(pos: String): TextView {
    val textView = LayoutInflater.from(context).inflate(
      R.layout.details_part_of_speech_layout,
      binding.definitionsListContainer,
      false
    ) as TextView
    textView.text = pos
    return textView
  }

  private fun createDefinitionView(def: String): TextView {
    val textView = LayoutInflater.from(context).inflate(
      R.layout.details_definition_layout,
      binding.definitionsListContainer,
      false
    ) as AppCompatTextView
    textView.text = ":$def"
    return textView
  }

  private fun createExampleView(example: String): TextView {
    val textView = LayoutInflater.from(context).inflate(
      R.layout.details_example_layout,
      binding.examplesListContainer,
      false
    ) as TextView
    textView.text = example.trimStart().trimEnd()
    return textView
  }
}