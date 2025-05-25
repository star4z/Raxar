package com.example.raxar.ui.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val noteRepository: NoteRepository,
) : ViewModel() {

  private val args = NoteDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

  val note: LiveData<NoteDto> =
    noteRepository.getNote(args.noteId).map {
      // If note doesn't exist, create a temp one in memory to be saved later
      it ?: NoteDto(
        parentNoteId = args.parentNoteId.getDefaultForId(),
        source = NoteDto.Source.MEMORY
      )
    }

  suspend fun saveNote(noteDetailDto: NoteDetailDto): NoteDto {
    return withContext(Dispatchers.IO) {
      val noteDto = getNoteDto(noteDetailDto)
      if (noteDto.source == NoteDto.Source.MEMORY) {
        return@withContext noteRepository.createNote(noteDto)
      } else {
        noteRepository.updateNote(noteDto)
        return@withContext noteDto
      }
    }
  }

  private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto {
    val noteValue = note.value!!
    return noteValue.copy(title = noteDetailDto.title, body = noteDetailDto.body)
  }

  fun deleteNote(noteDto: NoteDto) {
    viewModelScope.launch(Dispatchers.IO) {
      noteRepository.deleteNote(noteDto)
    }
  }

  private fun Long.getDefaultForId(): Long? = if (this != 0L) this else null
}