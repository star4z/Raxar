package com.example.raxar.data

import com.example.raxar.data.dbviews.NoteWithCurrentCommitView
import com.example.raxar.data.models.Note
import com.example.raxar.data.models.NoteCommit
import com.example.raxar.data.pojos.NoteWithCommits
import com.example.raxar.data.pojos.NoteWithCurrentCommitAndChildNotes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    //region Repository methods

    fun getRootNotes(): Flow<List<NoteDto>> {
        return noteDao.getNotesWithCommitsForRootNode().map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }

    fun getNote(id: Long): Flow<NoteDto?> {
        Timber.d("getNote(${id})")
        return noteDao.getNoteWithCurrentCommitAndChildNotes(id)
            .map(this::nullableNoteWithCurrentCommitAndChildNotesToNoteDto)
    }

    fun insertNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        noteDao.insertNoteAndNoteCommit(noteWithCommits.note, noteDto.currentNoteCommit)
    }

    fun updateNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        noteDao.updateNoteAndInsertNoteCommit(noteWithCommits.note, noteDto.currentNoteCommit)
    }

    fun deleteNote(noteDto: NoteDto) {
        val note = noteDtoToNote(noteDto)
        noteDao.deleteNote(note)
    }

    //endregion

    //region Mappers

    private fun nullableNoteWithCurrentCommitAndChildNotesToNoteDto(
        noteWithCurrentCommitAndChildNotes: NoteWithCurrentCommitAndChildNotes
    ): NoteDto {
        return NoteDto(
            noteId = noteWithCurrentCommitAndChildNotes.note.noteId,
            parentNoteId = noteWithCurrentCommitAndChildNotes.note.parentNoteId,
            currentNoteCommit = noteWithCurrentCommitAndChildNotes.noteCommit,
            childNotes = noteWithCurrentCommitAndChildNotes.childNotes.map(this::noteWithCurrentCommitViewToNoteDto)
        )
    }

    private fun noteWithCurrentCommitViewToNoteDto(noteWithCurrentCommitView: NoteWithCurrentCommitView): NoteDto {
        return NoteDto(
            noteId = noteWithCurrentCommitView.noteId,
            parentNoteId = noteWithCurrentCommitView.parentNoteId,
            currentNoteCommit = noteWithCurrentCommitViewToNoteCommit(noteWithCurrentCommitView)
        )
    }

    private fun noteWithCurrentCommitViewToNoteCommit(noteWithCurrentCommitView: NoteWithCurrentCommitView): NoteCommit {
        return NoteCommit(
            noteCommitId = noteWithCurrentCommitView.currentNoteCommitId,
            noteId = noteWithCurrentCommitView.noteId,
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
            currentNoteCommitId = noteDto.currentNoteCommit.noteCommitId
        )
    }

    private fun noteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits): NoteDto {
        val noteCommits = noteWithCommits.noteCommits
        return NoteDto(
            noteId = noteWithCommits.note.noteId,
            parentNoteId = noteWithCommits.note.parentNoteId,
            currentNoteCommit = noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }!!,
            noteCommits = noteCommits
        )
    }

    private fun nullableNoteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits?): NoteDto? {
        noteWithCommits?.let {
            val noteCommits = noteWithCommits.noteCommits
            return NoteDto(
                noteId = noteWithCommits.note.noteId,
                parentNoteId = noteWithCommits.note.parentNoteId,
                currentNoteCommit = noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }!!,
                noteCommits = noteCommits
            )
        } ?: return null
    }

    private fun noteDtoToNoteWithCommits(noteDto: NoteDto): NoteWithCommits {
        return NoteWithCommits(
            noteDtoToNote(noteDto),
            noteDto.noteCommits
        )
    }

    //endregion
}