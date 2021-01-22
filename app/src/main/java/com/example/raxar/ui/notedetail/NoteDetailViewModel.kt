package com.example.raxar.ui.notedetail

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.raxar.data.NoteCommit
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.time.ZonedDateTime

class NoteDetailViewModel @ViewModelInject constructor(
    private val noteRepository: NoteRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {
    private val random = SecureRandom()
    private var created = false
    private val noteIdFromState = savedStateHandle.get<Long>("noteId")!!
    private var noteId: Long = 0L

    init {
        if (noteId == 0L) {
            if (noteIdFromState == 0L) {
                noteId = genId()
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

    var childNotes =
        if (created) {
            MutableLiveData(listOf())
        } else {
            noteRepository.getChildNotes(noteId).asLiveData()
        }

    fun saveNote(noteDetailDto: NoteDetailDto): NoteDto? {
        val noteDto = getNoteDto(noteDetailDto)
        noteDto?.let {
            viewModelScope.launch(Dispatchers.IO) {
                noteRepository.saveNote(noteDto)
                if (created) {
                    note = noteRepository.getNote(noteId).asLiveData()
                    childNotes = noteRepository.getChildNotes(noteId).asLiveData()
                    created = false
                }
            }
        }
        return noteDto
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto? {
        note.value?.let { noteDto: NoteDto ->
            val noteCommit = NoteCommit(
                genId(),
                noteDto.noteId,
                noteDto.currentNoteCommit.noteCommitId,
                ZonedDateTime.now(),
                "",
                noteDetailDto.title,
                noteDetailDto.body
            )
            return NoteDto(
                noteDto.noteId,
                if (noteDetailDto.parentNoteId == 0L) null else noteDetailDto.parentNoteId,
                noteCommit,
                noteDto.noteCommits + noteCommit
            )
        } ?: run {
            return null
        }
    }

    private fun genId(): Long {
        var id = random.nextLong()
        while (id == 0L) {
            id = random.nextLong()
        }
        return id
    }

    fun deleteNote(noteDto: NoteDto) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(noteDto)
        }
    }
}