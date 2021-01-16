package com.example.raxar.ui.notes

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import kotlinx.coroutines.launch

class NotesViewModel @ViewModelInject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {
    val notes: LiveData<List<NoteDto>> = noteRepository.getChildNotes(0L).asLiveData()

    fun deleteNote(noteDto: NoteDto) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteDto)
        }
    }

}