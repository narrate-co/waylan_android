package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.narrate.waylan.android.R
import space.narrate.waylan.core.data.Result
import space.narrate.waylan.core.data.firestore.users.UserWordExample
import space.narrate.waylan.core.repo.WordRepository
import space.narrate.waylan.core.ui.common.Event

class WaylanExamplesDetailViewModel(
  private val wordRepository: WordRepository
): ViewModel() {

  private var data: WaylanExamplesModel? = null

  private var exampleUnderEdit: UserWordExample? = null

  private val _examples = MutableLiveData<List<UserWordExample>>()
  val examples: LiveData<List<UserWordExample>>
    get() = _examples

  private val _shouldShowEditor: MutableLiveData<UserWordExample?> = MutableLiveData()
  val shouldShowEditor: LiveData<UserWordExample?>
    get() = _shouldShowEditor

  private val _shouldFocusEditor: MutableLiveData<Event<Boolean>> = MutableLiveData()
  val shouldFocusEditor: LiveData<Event<Boolean>>
    get() = _shouldFocusEditor

  private val _shouldShowEditorError: MutableLiveData<String?> = MutableLiveData()
  val shouldShowEditorError: LiveData<String?>
    get() = _shouldShowEditorError

  private val _shouldShowMessage: MutableLiveData<Int?> = MutableLiveData()
  val shouldShowMessage: LiveData<Int?>
    get() = _shouldShowMessage

  private val _showLoading: MutableLiveData<Boolean> = MutableLiveData()
  val showLoading: LiveData<Boolean>
    get() = _showLoading

  private val _shouldShowDestructiveButton: MutableLiveData<Boolean> = MutableLiveData()
  val shouldShowDestructiveButton: LiveData<Boolean>
    get() = _shouldShowDestructiveButton

  private val _shouldCloseKeyboard: MutableLiveData<Event<Boolean>> = MutableLiveData()
  val shouldCloseKeyboard: LiveData<Event<Boolean>>
    get() = _shouldCloseKeyboard

  fun setData(data: WaylanExamplesModel) {
    if (this.data?.isSameAs(data) == true && this.data?.isContentSameAs(data) == true) return

    // Reset editor and other fields when a new word is supplied
    if (data.word != this.data?.word) {
      exampleUnderEdit = null
      _shouldShowEditor.value = null
      _shouldShowEditorError.value = null
    }

    this.data = data
    _examples.value = data.examples

    // If there are no examples, prompt user to add a custom entry
    updateShouldShowMessage()
  }

  fun onEditExampleClicked(example: UserWordExample) {
    val examples = _examples.value ?: return
    // Remove example from examples list during editing
    val filteredExamples = examples.filter { it.id != example.id }.toList()
    _examples.value = filteredExamples
    // Set as the example under edit
    openEditorFor(example)
  }

  fun onCreateExampleClicked() {
    // Create new user word example.
    val newExample = UserWordExample()
    openEditorFor(newExample)
  }

  private fun openEditorFor(example: UserWordExample) {
    // Set editor to visible
    exampleUnderEdit = example
    _shouldShowEditor.value = exampleUnderEdit
    _shouldFocusEditor.value = Event(true)
    _shouldShowDestructiveButton.value = example.id.isNotEmpty()
    updateShouldShowMessage()
  }

  fun onPositiveEditorButtonClicked() = viewModelScope.launch {
    _showLoading.value = true
    _shouldCloseKeyboard.value = Event(true)
    // Set the current word
    val example = exampleUnderEdit
    val data = data
    if (example != null && data != null) {
      val result = wordRepository.updateUserWordExample(data.word, example)
      when (result) {
        is Result.Success -> {
          _shouldShowEditor.value = null
          exampleUnderEdit = null
        }
        is Result.Error -> {
          _shouldShowEditorError.value = result.exception.message ?: "Something went wrong."
        }
      }
    }

    _showLoading.value = false
    updateShouldShowMessage()
  }

  fun onNegativeEditorButtonClicked() {
    // Close editor
    _shouldShowEditor.value = null
    _shouldCloseKeyboard.value = Event(true)
    exampleUnderEdit = null
    // Restore all examples to the list if any were removed during editing.
    _examples.value = this.data?.examples ?: emptyList()
    // Update the message if the examples are empty, etc.
    updateShouldShowMessage()
  }

  fun onDestructiveEditorButtonClicked() = GlobalScope.launch {
    _shouldCloseKeyboard.value = Event(true)
    val data = data
    val example = exampleUnderEdit
    if (data != null && example != null) {
      wordRepository.deleteUserWordExample(data.word, example.id)
    }

    withContext(Dispatchers.Main) {
      _shouldShowEditor.value = null
      exampleUnderEdit = null
      updateShouldShowMessage()
    }
  }

  fun onEditorTextChanged(
      text: CharSequence?,
      start: Int,
      before: Int,
      count: Int
  ) {
    // TODO: Possibly validate example text.
    // This is difficult because the example might include the root/prefixed/suffixed variations
    // of the entry. Additionally, there is no reason to limit what users can add as their own
    // helpers.
    exampleUnderEdit?.apply {
      example = text?.toString() ?: example
      modified = Date()
    }
  }

  private fun updateShouldShowMessage() {
    if (_shouldShowEditor.value == null && _examples.value.isNullOrEmpty()) {
      _shouldShowMessage.value = R.string.details_waylan_example_no_examples_message
    } else {
      _shouldShowMessage.value = null
    }
  }
}