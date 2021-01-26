package space.narrate.waylan.android.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import space.narrate.waylan.android.R
import space.narrate.waylan.android.databinding.EntryEditTextLayoutBinding

class EntryEditTextView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  val binding: EntryEditTextLayoutBinding =
    EntryEditTextLayoutBinding.inflate(LayoutInflater.from(context), this)

  init {
    background = AppCompatResources.getDrawable(
      context,
      R.drawable.text_input_background_transitionable
    )
  }

  fun setOnPositiveButtonClickListener(onClick: OnClickListener) {
    binding.positiveButton.setOnClickListener(onClick)
  }

  fun setOnNegativeButtonClickListener(onClick: OnClickListener) {
    binding.negativeButton.setOnClickListener(onClick)
  }

  fun setOnDestructiveButtonClickListener(onClick: OnClickListener) {
    binding.destructiveButton.setOnClickListener(onClick)
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

}