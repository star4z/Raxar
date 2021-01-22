package com.example.raxar.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    private fun noteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits): NoteDto {
        val noteCommits = noteWithCommits.noteCommits
        return NoteDto(
            noteWithCommits.note.noteId,
            noteWithCommits.note.parentNoteId,
            noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }!!,
            noteCommits
        )
    }

    private fun nullableNoteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits?): NoteDto? {
        noteWithCommits?.let {
            val noteCommits = noteWithCommits.noteCommits
            return NoteDto(
                noteWithCommits.note.noteId,
                noteWithCommits.note.parentNoteId,
                noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }!!,
                noteCommits
            )
        } ?: return null
    }

    fun insertNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        noteDao.insertNoteAndNoteCommit(noteWithCommits.note, noteDto.currentNoteCommit)
    }

    fun updateNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        noteDao.updateNoteAndInsertNoteCommit(noteWithCommits.note, noteDto.currentNoteCommit)
    }

    private fun noteDtoToNoteWithCommits(noteDto: NoteDto): NoteWithCommits {
        return NoteWithCommits(
            Note(
                noteDto.noteId,
                noteDto.parentNoteId,
                noteDto.currentNoteCommit.noteCommitId
            ),
            noteDto.noteCommits
        )
    }

    fun getNote(id: Long): Flow<NoteDto?> {
        Timber.d("getNote(${id})")
        return noteDao.getNotesWithCommits(id).map(this::nullableNoteWithCommitsToNoteDto)
    }

    fun deleteNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        deleteNote(noteWithCommits)
    }

    private fun deleteNote(noteWithCommits: NoteWithCommits) {
        noteDao.deleteNote(noteWithCommits.note)
    }

    fun getRootNotes(): Flow<List<NoteDto>> {
        return noteDao.getNotesWithCommitsForRootNode().map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }

    fun getChildNotes(parentId: Long): Flow<List<NoteDto>> {
        Timber.d("getChildNotes(${parentId}")
        return noteDao.getNotesWithCommitsForParentId(parentId).map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }
}