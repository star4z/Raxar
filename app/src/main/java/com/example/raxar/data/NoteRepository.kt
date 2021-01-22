package com.example.raxar.data

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

    fun getNote(id: Long): Flow<NoteDto?> {
        Timber.d("getNote(${id})")
        return noteDao.getNoteWithCurrentCommitAndChildNotes(id)
            .map(this::nullableNoteWithCurrentCommitAndChildNotesToNoteDto)
    }

    fun insertNote(noteDto: NoteDto) {
        val note = noteDtoToNote(noteDto)
        val commit = noteDtoToNoteCommit(noteDto)
        noteDao.insertNoteAndNoteCommit(note, commit)
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
        noteWithCurrentCommitAndChildNotes: NoteWithCurrentCommitAndChildNotes?
    ): NoteDto? {
        noteWithCurrentCommitAndChildNotes?.let {
            return NoteDto(
                noteId = noteWithCurrentCommitAndChildNotes.note.noteId,
                parentNoteId = noteWithCurrentCommitAndChildNotes.note.parentNoteId,
                noteCommitId = noteWithCurrentCommitAndChildNotes.noteCommit.noteCommitId,
                parentNoteCommitId = noteWithCurrentCommitAndChildNotes.noteCommit.parentNoteCommitId,
                time = noteWithCurrentCommitAndChildNotes.noteCommit.time,
                color = noteWithCurrentCommitAndChildNotes.noteCommit.color,
                title = noteWithCurrentCommitAndChildNotes.noteCommit.title,
                body = noteWithCurrentCommitAndChildNotes.noteCommit.body,
                childNotes = noteWithCurrentCommitAndChildNotes.childNotes.map(this::noteWithCurrentCommitViewToNoteDto)
            )
        } ?: return null
    }

    private fun noteWithCurrentCommitViewToNoteDto(noteWithCurrentCommitView: NoteWithCurrentCommitView): NoteDto {
        return NoteDto(
            noteId = noteWithCurrentCommitView.noteId,
            parentNoteId = noteWithCurrentCommitView.parentNoteId,
            noteCommitId = noteWithCurrentCommitView.noteCommitId,
            parentNoteCommitId = noteWithCurrentCommitView.parentNoteCommitId,
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
            noteCommitId = noteDto.noteCommitId,
            noteId = noteDto.noteId,
            parentNoteCommitId = noteDto.parentNoteCommitId,
            time = noteDto.time,
            color = noteDto.color,
            title = noteDto.title,
            body = noteDto.body
        )
    }

    //endregion
}