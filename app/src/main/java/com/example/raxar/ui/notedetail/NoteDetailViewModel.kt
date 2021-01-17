package com.example.raxar.ui.notedetail

import android.os.Bundle
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.raxar.data.NoteCommit
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.time.ZonedDateTime

class NoteDetailViewModel @ViewModelInject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {
    lateinit var note: LiveData<NoteDto>
    lateinit var childNotes: LiveData<List<NoteDto>>
    private var state: Bundle = Bundle()

    private val random = SecureRandom()

    fun getNote(id: Long, fromRepo: Boolean) {
        if (fromRepo) {
            note = noteRepository.getNote(id).asLiveData()
            childNotes = noteRepository.getChildNotes(id).asLiveData()
        } else {
            note = MutableLiveData(NoteDto(id))
            childNotes = MutableLiveData(listOf())
        }
    }

    fun getState(): Bundle {
        return state
    }

    fun saveNote(noteDetailDto: NoteDetailDto): NoteDto {
        val noteDto = getNoteDto(noteDetailDto)
        noteRepository.saveNote(noteDto)
        return noteDto
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto {
        note.value!!.let { noteDto: NoteDto ->
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
                noteDetailDto.parentNoteId,
                noteCommit,
                noteDto.noteCommits + noteCommit
            )
        }
    }

    fun genId(): Long {
        var id = random.nextLong()
        while (id == 0L) {
            id = random.nextLong()
        }
        return id
    }

    fun deleteNote(noteDto: NoteDto) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteDto)
        }
    }
}