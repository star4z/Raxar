package com.example.raxar.ui.notepreview

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository

class NoteDetailViewModel @ViewModelInject constructor(private val noteRepository: NoteRepository) :
    ViewModel() {
    lateinit var note: LiveData<NoteDto>

    fun getNote(id: Long) {
        note = noteRepository.getNote(id).asLiveData()
    }
}