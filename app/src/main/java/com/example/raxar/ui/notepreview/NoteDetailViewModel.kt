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

    private val random = SecureRandom()

    fun getNote(id: Long) {
        note = if (id != 0L) {
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
                genId(),
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
            val noteId = genId()
            val noteCommit = NoteCommit(
                genId(),
                noteId,
                0L,
                ZonedDateTime.now(),
                "",
                noteDetailDto.title,
                noteDetailDto.body
            )
            return NoteDto(
                noteId,
                noteDetailDto.parentNoteId,
                noteCommit,
                listOf(noteCommit)
            )
        }
    }

    private fun genId(): Long {
        var id = random.nextLong()
        while (id == 0L) {
            id = random.nextLong()
        }
        return id
    }
}