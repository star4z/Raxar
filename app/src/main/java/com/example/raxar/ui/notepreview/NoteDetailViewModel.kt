package com.example.raxar.ui.notepreview

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.raxar.data.NoteCommit
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import java.security.SecureRandom
import java.time.ZonedDateTime

class NoteDetailViewModel @ViewModelInject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {
    lateinit var note: LiveData<NoteDto>

    fun getNote(id: Long) {
        note = if (id > 0L) {
            noteRepository.getNote(id).asLiveData()
        } else {
            MutableLiveData()
        }
    }

    fun saveNote(noteDetailDto: NoteDetailDto) {
        val noteDto = getNoteDto(noteDetailDto)
        noteRepository.saveNote(noteDto).also { getNote(noteDto.noteId) }
    }

    private fun getNoteDto(noteDetailDto: NoteDetailDto): NoteDto {
        note.value?.let {
            val noteCommit = NoteCommit(
                SecureRandom().nextLong(),
                it.noteId,
                it.currentNoteCommit.noteCommitId,
                ZonedDateTime.now(),
                "",
                noteDetailDto.title,
                noteDetailDto.body
            )
            return NoteDto(
                it.noteId,
                it.parentId,
                noteCommit,
                it.noteCommits + noteCommit
            )
        } ?: run {
            val noteId = SecureRandom().nextLong()
            val noteCommit = NoteCommit(
                SecureRandom().nextLong(),
                noteId,
                0L,
                ZonedDateTime.now(),
                "",
                noteDetailDto.title,
                noteDetailDto.body
            )
            return NoteDto(
                noteId,
                0L,
                noteCommit,
                listOf(noteCommit)
            )
        }
    }
}