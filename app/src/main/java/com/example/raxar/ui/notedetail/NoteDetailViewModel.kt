package com.example.raxar.ui.notedetail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import com.example.raxar.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteDetailViewModel @ViewModelInject constructor(
    private val noteRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val idGenerator: IdGenerator
) : ViewModel() {

    var needToCreate = false
        private set
    private val noteIdFromState = savedStateHandle.get<Long>("noteId")!!
    private val parentNoteIdFromState = savedStateHandle.get<Long>("parentNoteId")!!
    private var noteId: Long = 0L

    init {
        if (noteId == 0L) {
            if (noteIdFromState == 0L) {
                noteId = idGenerator.genId()
                needToCreate = true
            } else {
                noteId = noteIdFromState
                needToCreate = false
            }
        }
    }

    var note: LiveData<NoteDto?> =
        if (needToCreate) {
            MutableLiveData(NoteDto(noteId))
        } else {
            noteRepository.getNote(noteId).asLiveData()
        }

    fun saveNote(noteDetailDto: NoteDetailDto): NoteDto? {
        val noteDto = getNoteDto(noteDetailDto)
        noteDto?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (needToCreate) {
                    noteRepository.insertNote(noteDto)
                    note = noteRepository.getNote(noteId).asLiveData()
                    needToCreate = false
                } else {
                    noteRepository.updateNote(noteDto)
                }
            }
        }
        return noteDto
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto? {
        note.value?.let { noteDto: NoteDto ->
            return NoteDto(
                noteId = noteDto.noteId,
                parentNoteId = if (parentNoteIdFromState != 0L) parentNoteIdFromState else null,
                noteCommitId = idGenerator.genId(),
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
}