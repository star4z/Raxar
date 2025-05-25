package com.example.raxar.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

  //region Repository methods

  fun getRootNotes(): Flow<List<NoteDto>> {
    return noteDao.getNotesWithCurrentCommitsForRootNode().map {
      it.map(this::noteWithCurrentCommitViewToNoteDto)
    }
  }

  fun getNote(id: Long): LiveData<NoteDto?> {
    Timber.d("getNote(${id})")
    return noteDao.getNoteWithCurrentCommitAndChildNotes(id)
      .map(this::nullableNoteWithCurrentCommitAndChildNotesToNoteDto)
  }

  fun createNote(noteDto: NoteDto): NoteDto {
    val note = noteDtoToNote(noteDto)
    val commit = noteDtoToNoteCommit(noteDto)
    try {
      return nullableNoteWithCurrentCommitAndChildNotesToNoteDto(
        noteDao.createNote(
          note,
          commit
        )
      )!!
    } catch (e: Exception) {
      Timber.e(e)
      throw e
    }
  }

  fun updateNote(noteDto: NoteDto) {
    val note = noteDtoToNote(noteDto)
    val commit = noteDtoToNoteCommit(noteDto)
    noteDao.updateNoteAndInsertNoteCommit(note, commit)
  }

  fun deleteNote(noteDto: NoteDto) {
    val note = noteDtoToNote(noteDto)
    noteDao.deleteNote(note)
  }

  //endregion

  //region Mappers

  private fun nullableNoteWithCurrentCommitAndChildNotesToNoteDto(
    noteWithCurrentCommitAndChildNotes: NoteWithCurrentCommitAndChildNotes?,
  ): NoteDto? {
    Timber.d("note(+)=${noteWithCurrentCommitAndChildNotes}")
    noteWithCurrentCommitAndChildNotes?.let {
      return NoteDto(
        noteId = noteWithCurrentCommitAndChildNotes.note.noteId,
        parentNoteId = noteWithCurrentCommitAndChildNotes.note.parentNoteId,
        noteCommitId = noteWithCurrentCommitAndChildNotes.noteCommit.noteCommitId,
        time = noteWithCurrentCommitAndChildNotes.noteCommit.time,
        color = noteWithCurrentCommitAndChildNotes.noteCommit.color,
        title = noteWithCurrentCommitAndChildNotes.noteCommit.title,
        body = noteWithCurrentCommitAndChildNotes.noteCommit.body,
        childNotes = noteWithCurrentCommitAndChildNotes.childNotes.map(
          this::noteWithCurrentCommitViewToNoteDto
        )
      )
    } ?: return null
  }

  private fun noteWithCurrentCommitViewToNoteDto(noteWithCurrentCommitView: NoteWithCurrentCommitView): NoteDto {
    return NoteDto(
      noteId = noteWithCurrentCommitView.noteId,
      parentNoteId = noteWithCurrentCommitView.parentNoteId,
      noteCommitId = noteWithCurrentCommitView.noteCommitId,
      time = noteWithCurrentCommitView.time,
      color = noteWithCurrentCommitView.color,
      title = noteWithCurrentCommitView.title,
      body = noteWithCurrentCommitView.body
    )
  }

  private fun noteDtoToNote(noteDto: NoteDto): Note {
    return Note(
      noteId = noteDto.noteId,
      parentNoteId = noteDto.parentNoteId,
      currentNoteCommitId = noteDto.noteCommitId
    )
  }

  private fun noteDtoToNoteCommit(noteDto: NoteDto): NoteCommit {
    return NoteCommit(
      noteId = noteDto.noteId,
      time = noteDto.time,
      color = noteDto.color,
      title = noteDto.title,
      body = noteDto.body
    )
  }

  //endregion
}