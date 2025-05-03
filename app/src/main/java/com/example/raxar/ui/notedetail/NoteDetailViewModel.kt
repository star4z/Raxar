package com.example.raxar.ui.notedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository
) : ViewModel() {

    var needToCreate = false
        private set
    private val noteIdFromState = savedStateHandle.get<Long>("noteId")!!
    private val parentNoteIdFromState = savedStateHandle.get<Long>("parentNoteId")!!


    val note: LiveData<NoteDto?> = if (noteIdFromState == 0L) {
        runBlocking(Dispatchers.IO) {
            noteRepository.createNote(parentNoteIdFromState.getDefaultForId())
                .asLiveData(Dispatchers.IO)
        }
    } else {
        noteRepository.getNote(noteIdFromState).asLiveData(Dispatchers.IO)
    }


    fun saveNote(noteDetailDto: NoteDetailDto): NoteDto? {
        val noteDto = getNoteDto(noteDetailDto)
        noteDto?.let {
            viewModelScope.launch(Dispatchers.IO) {
                noteRepository.updateNote(noteDto)
            }
        }
        return noteDto
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto? {
        note.value?.let { noteDto: NoteDto ->
            return NoteDto(
                noteId = noteDto.noteId,
                parentNoteId = noteDto.parentNoteId,
                parentNoteCommitId = noteDto.noteCommitId,
                title = noteDetailDto.title,
                body = noteDetailDto.body
            )
        } ?: run {
            return null
        }
    }

    fun deleteNote(noteDto: NoteDto) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(noteDto)
        }
    }

    private fun Long.getDefaultForId(): Long? = if (this != 0L) this else null
}