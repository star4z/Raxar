package com.example.raxar.ui.notes

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository

class NotesViewModel @ViewModelInject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {
    val notes: LiveData<List<NoteDto>> = noteRepository.getNotes().asLiveData()

    suspend fun addNote(noteDto: NoteDto) {
        noteRepository.saveNote(noteDto)
    }
}