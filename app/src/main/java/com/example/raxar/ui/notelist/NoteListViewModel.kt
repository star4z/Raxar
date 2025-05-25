package com.example.raxar.ui.notelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.raxar.data.NoteDto
import com.example.raxar.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteListViewModel @Inject constructor(private val noteRepository: NoteRepository) :
  ViewModel() {
  val notes: LiveData<List<NoteDto>> = noteRepository.getRootNotes().asLiveData()

  fun deleteNote(noteDto: NoteDto) {
    viewModelScope.launch(Dispatchers.IO) {
      noteRepository.deleteNote(noteDto)
    }
  }
}