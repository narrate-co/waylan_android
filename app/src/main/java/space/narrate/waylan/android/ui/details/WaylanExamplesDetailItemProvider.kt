package space.narrate.waylan.android.ui.details

import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.DetailsWaylanExamplesItemLayoutBinding
import space.narrate.waylan.core.data.firestore.users.UserWordExample
import space.narrate.waylan.core.data.wordset.Example
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.core.util.invisible
import space.narrate.waylan.core.util.visible
import space.narrate.waylan.core.R as coreR

/**
 * An item provider which knows how to create a ViewHolder for the [DetailItemType.EXAMPLE]
 * item type
 */
class WaylanExampleDetailItemProvider(
    private val wordRepository: WordRepository
) : DetailItemProvider {
    override val itemType: DetailItemType = DetailItemType.EXAMPLE

    override fun createViewHolder(
        parent: ViewGroup,
        listener: DetailAdapterListener
    ): DetailItemViewHolder {
        return WaylanExampleViewHolder(
            DetailsWaylanExamplesItemLayoutBinding.inflate(parent.inflater, parent, false),
            WaylanExamplesDetailViewModel(wordRepository),
            listener
        )
    }
}

class WaylanExampleViewHolder(
    private val binding: DetailsWaylanExamplesItemLayoutBinding,
    private val viewModel: WaylanExamplesDetailViewModel,
    val listener: DetailAdapterListener
): DetailItemViewHolder(
    binding.root
) {

    override fun bind(item: DetailItemModel) {
        if (item !is WaylanExamplesModel) return
        viewModel.setData(item)

        // Add and keep examples list updated
        viewModel.examples.observe(this) {
            println("WaylanExamples set examples")
            setExamples(it)
        }

        // Watch for when and what the message box should display
        viewModel.shouldShowMessage.observe(this) {
            println("WaylanExamples shouldShowMessage = $it")
            if (it.isNullOrEmpty()) {
                binding.messageContainer.gone()
            } else {
                binding.messageTextView.text = it
                binding.messageContainer.visible()
            }
        }

        // Watch for when the editor should be shown
        viewModel.shouldShowEditor.observe(this) {
            if (it != null) {
                binding.entryEditTextView.visible()
                binding.entryEditTextView.setText(it.example)
                // TODO: Seed example visibility label
            } else {
                binding.entryEditTextView.gone()
            }
        }

        viewModel.shouldShowEditorError.observe(this) {
          it.withUnhandledContent {
            binding.run {
                (entryEditTextView.background as TransitionDrawable).apply {
                    isCrossFadeEnabled = true
                    if (it.isEmpty()) reverseTransition(200) else startTransition(200)
                }
            }
          }
        }

        // Pass actions through to view model
        binding.run {
            actionView.setOnClickListener { viewModel.onCreateExampleClicked() }
            entryEditTextView.onTextChanged(viewModel::onEditorTextChanged)
            entryEditTextView.setOnPositiveButtonClickListener {
                viewModel.onPositiveEditorButtonClicked()
            }
            entryEditTextView.setOnNegativeButtonClickListener {
                viewModel.onNegativeEditorButtonClicked()
            }
            entryEditTextView.setOnDestructiveButtonClickListener {
                viewModel.onDestructiveEditorButtonClicked()
            }
        }
    }

    private fun setExamples(examples: List<UserWordExample>) {
      binding.run {
        examplesContainer.removeAllViews()
        //add examples
        if (examples.isNotEmpty()) {
            // Loop to create and add each example
            examples.forEach {
                examplesContainer.addView(createExampleView(it))
            }
        }
      }
    }

    private fun createExampleView(example: UserWordExample): View {
        val view = LayoutInflater.from(binding.examplesContainer.context).inflate(
            R.layout.waylan_example_item_layout,
            binding.examplesContainer,
            false
        )
        val tv: TextView = view.findViewById(R.id.example_text_view)
        tv.text = example.example
        return view
    }
}
