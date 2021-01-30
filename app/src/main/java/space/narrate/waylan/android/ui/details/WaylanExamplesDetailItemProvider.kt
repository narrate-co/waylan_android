package space.narrate.waylan.android.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import space.narrate.waylan.android.databinding.DetailsWaylanExamplesItemLayoutBinding
import space.narrate.waylan.android.databinding.WaylanExampleItemLayoutBinding
import space.narrate.waylan.core.data.firestore.users.UserWordExample
import space.narrate.waylan.core.details.DetailAdapterListener
import space.narrate.waylan.core.details.DetailItemModel
import space.narrate.waylan.core.details.DetailItemProvider
import space.narrate.waylan.core.details.DetailItemType
import space.narrate.waylan.core.details.DetailItemViewHolder
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.hideIme
import space.narrate.waylan.core.util.inflater
import space.narrate.waylan.core.util.showIme
import space.narrate.waylan.core.util.visible

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

/**
 * A view holder that handles showing an Example section where users are able to add their own
 * custom examples per entry.
 */
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
            setExamples(it)
        }

        // Watch for when and what the message box should display
        viewModel.shouldShowMessage.observe(this) {
            if (it == null) {
                binding.messageContainer.gone()
            } else {
                binding.messageTextView.setText(it)
                binding.messageContainer.visible()
            }
        }

        // Watch for when the editor should be shown
        viewModel.shouldShowEditor.observe(this) {
            val transition = ChangeBounds()
            TransitionManager.beginDelayedTransition(binding.root, transition)
            if (it != null) {
                binding.entryEditTextView.visible()
                binding.entryEditTextView.setText(it.example)
                // TODO: Seed example visibility label
            } else {
                binding.entryEditTextView.gone()
            }
        }

        viewModel.shouldFocusEditor.observe(this) { event ->
            event.withUnhandledContent {
                // request focus and show keyboard
                if (it) binding.entryEditTextView.requestFocusForEditor()
                binding.entryEditTextView.getEditableView().showIme()
            }
        }

        viewModel.shouldCloseKeyboard.observe(this) { event ->
            event.withUnhandledContent {
                if (it) binding.entryEditTextView.getEditableView().hideIme()
            }
        }

        viewModel.shouldShowEditorError.observe(this) {
            binding.entryEditTextView.setError(it)
        }

        viewModel.shouldShowDestructiveButton.observe(this) {
            binding.entryEditTextView.showDestructiveButton(it)
        }

        viewModel.showLoading.observe(this) {
            binding.actionView.setLoading(it)
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
        val exampleBinding = WaylanExampleItemLayoutBinding.inflate(
            LayoutInflater.from(binding.examplesContainer.context),
            binding.examplesContainer,
            false
        )
        exampleBinding.exampleTextView.text = example.example
        exampleBinding.exampleTrailingIcon.setOnClickListener {
            viewModel.onEditExampleClicked(example)
        }
        return exampleBinding.root
    }
}
