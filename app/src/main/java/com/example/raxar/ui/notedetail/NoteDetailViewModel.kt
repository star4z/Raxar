package com.example.raxar.ui.notedetail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import com.example.raxar.data.models.NoteCommit
import com.example.raxar.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class NoteDetailViewModel @ViewModelInject constructor(
    private val noteRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val idGenerator: IdGenerator
) :
    ViewModel() {
    private var created = false
    private val noteIdFromState = savedStateHandle.get<Long>("noteId")!!
    private val parentNoteIdFromState = savedStateHandle.get<Long>("parentNoteId")!!
    private var noteId: Long = 0L

    init {
        if (noteId == 0L) {
            if (noteIdFromState == 0L) {
                noteId = idGenerator.genId()
                created = true
            } else {
                noteId = noteIdFromState
                created = false
            }
        }
    }

    var note: LiveData<NoteDto?> =
        if (created) {
            MutableLiveData(NoteDto(noteId))
        } else {
            noteRepository.getNote(noteId).asLiveData()
        }

    fun saveNote(noteDetailDto: NoteDetailDto): NoteDto? {
        val noteDto = getNoteDto(noteDetailDto)
        noteDto?.let {
            viewModelScope.launch(Dispatchers.IO) {
                if (created) {
                    noteRepository.insertNote(noteDto)
                    note = noteRepository.getNote(noteId).asLiveData()
                    created = false
                } else {
                    noteRepository.updateNote(noteDto)
                }
            }
        }
        return noteDto
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto? {
        note.value?.let { noteDto: NoteDto ->
            val noteCommit = NoteCommit(
                idGenerator.genId(),
                noteDto.noteId,
                noteDto.currentNoteCommit.noteCommitId,
                ZonedDateTime.now(),
                "",
                noteDetailDto.title,
                noteDetailDto.body
            )
            return NoteDto(
                noteDto.noteId,
                if (parentNoteIdFromState != 0L) parentNoteIdFromState else null,
                noteCommit,
                noteDto.noteCommits + noteCommit
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