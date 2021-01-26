package space.narrate.waylan.android.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Date
import kotlinx.coroutines.launch
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

  private val _shouldShowEditorError: MutableLiveData<Event<String>> = MutableLiveData()
  val shouldShowEditorError: LiveData<Event<String>>
    get() = _shouldShowEditorError

  private val _shouldShowMessage: MutableLiveData<String> = MutableLiveData()
  val shouldShowMessage: LiveData<String>
    get() = _shouldShowMessage

  private val _showLoading: MutableLiveData<Boolean> = MutableLiveData()
  val showLoading: LiveData<Boolean>
    get() = _showLoading

  fun setData(data: WaylanExamplesModel) {
    if (this.data?.isSameAs(data) == true && this.data?.isContentSameAs(data) == true) return

    this.data = data
    _examples.postValue(data.examples)
    if (this.data?.word != data.word) {
      exampleUnderEdit = null
      _shouldShowEditor.postValue(null)
      _shouldShowEditorError.postValue(Event(""))
    }

    // If there are no examples, prompt user to add a custom entry
    updateShouldShowMessage()
  }

  fun onEditExampleClicked(example: UserWordExample) {
    // Remove example from examples list
    // Set as the example under edit
  }

  fun onCreateExampleClicked() {
    // Create new user word example.
    exampleUnderEdit = UserWordExample()
    // Set editor to visible
    _shouldShowEditor.postValue(exampleUnderEdit)
    updateShouldShowMessage()
  }

  fun onPositiveEditorButtonClicked() = viewModelScope.launch {
    _showLoading.postValue(true)
    // Set the current word
    val example = exampleUnderEdit
    val data = data
    if (example != null && data != null) {
      val result = wordRepository.updateUserWordExample(data.word, example)
      when (result) {
        is Result.Success -> {
          _shouldShowEditor.postValue(null)
          exampleUnderEdit = null
        }
        is Result.Error -> {
          _shouldShowEditorError.postValue (
            Event(result.exception.message ?: "Something went wrong.")
          )
        }
      }
    }

    _showLoading.postValue(false)
  }

  fun onNegativeEditorButtonClicked() {
    _shouldShowEditor.postValue(null)
    exampleUnderEdit = null
  }

  fun onDestructiveEditorButtonClicked() {
    // TODO: Delete example
    if (_shouldShowEditorError.value?.peek().isNullOrEmpty()) {
      _shouldShowEditorError.postValue(Event("This is a destructive message"))
    } else {
      _shouldShowEditorError.postValue(Event(""))
    }
  }

  fun onEditorTextChanged(
      text: CharSequence?,
      start: Int,
      before: Int,
      count: Int
  ) {
    // TODO: Validate example text?
    exampleUnderEdit?.apply {
      example = text?.toString() ?: example
      modified = Date()
    }
  }

  private fun updateShouldShowMessage() {
    if (_shouldShowEditorError.value == null && _examples.value?.isEmpty() == true) {
      _shouldShowMessage.postValue("No examples. Use the + button to add a custom example to this entry")
    } else {
      _shouldShowMessage.postValue(null)
    }
  }
}