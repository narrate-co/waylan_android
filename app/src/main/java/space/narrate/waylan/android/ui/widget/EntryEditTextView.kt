package space.narrate.waylan.android.ui.widget

import android.content.Context
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import space.narrate.waylan.android.databinding.EntryEditTextLayoutBinding
import space.narrate.waylan.core.util.gone
import space.narrate.waylan.core.util.visible

class EntryEditTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val binding: EntryEditTextLayoutBinding =
    EntryEditTextLayoutBinding.inflate(LayoutInflater.from(context), this)

  fun setOnPositiveButtonClickListener(onClick: OnClickListener) {
    binding.positiveButton.setOnClickListener(onClick)
  }

  fun setOnNegativeButtonClickListener(onClick: OnClickListener) {
    binding.negativeButton.setOnClickListener(onClick)
  }

  fun setOnDestructiveButtonClickListener(onClick: OnClickListener) {
    binding.destructiveButton.setOnClickListener(onClick)
  }

  fun showDestructiveButton(show: Boolean) {
    if (show) binding.destructiveButton.visible() else binding.destructiveButton.gone()
  }

  fun getText(): String = binding.editableTextView.text.toString()

  fun setText(text: String) = binding.editableTextView.setText(text)

  fun onTextChanged(
    action: (
      text: CharSequence?,
      start: Int,
      before: Int,
      count: Int
    ) -> Unit
  ) {
    binding.editableTextView.doOnTextChanged(action)
  }

  fun setError(message: String?) {
    val hasError = !message.isNullOrEmpty()
    binding.run {
      (boxContainer.background as TransitionDrawable).apply {
        isCrossFadeEnabled = true
        if (hasError) {
          startTransition(200)
        } else {
          resetTransition()
        }
      }

      if (hasError) {
        errorTextView.text = message
        errorTextView.visible()
      } else {
        errorTextView.gone()
      }
    }
  }

  fun requestFocusForEditor() {
    binding.editableTextView.requestFocus()
  }

  fun getEditableView(): EditText = binding.editableTextView
}