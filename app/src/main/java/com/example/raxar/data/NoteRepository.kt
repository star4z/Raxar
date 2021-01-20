package com.example.raxar.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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
            noteWithCommits.note.parentId,
            noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }!!,
            noteCommits
        )
    }

    suspend fun saveNote(noteDto: NoteDto): Long {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        val rowId = noteDao.saveNote(noteWithCommits.note)
        noteDao.saveNoteCommit(noteDto.currentNoteCommit)
        return rowId
    }

    private fun noteDtoToNoteWithCommits(noteDto: NoteDto): NoteWithCommits {
        return NoteWithCommits(
            Note(
                noteDto.noteId,
                noteDto.parentId,
                noteDto.currentNoteCommit.noteCommitId
            ),
            noteDto.noteCommits
        )
    }

    fun getNote(id: Long): Flow<NoteDto> {
        Timber.d("getNote(${id})")
        return noteDao.getNotesWithCommits(id).map(this::noteWithCommitsToNoteDto)
    }

    suspend fun deleteNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        deleteNote(noteWithCommits)
    }

    private suspend fun deleteNote(noteWithCommits: NoteWithCommits) {
        //Delete Commits before Note to prevent foreign key error
        noteDao.deleteNoteCommitsByNoteId(noteWithCommits.note.noteId)
        noteDao.deleteNote(noteWithCommits.note)
        noteDao.getNotesWithCommitsForParentId(noteWithCommits.note.noteId)
            .collect { notesWithCommits: List<NoteWithCommits> ->
                notesWithCommits.forEach {
                    deleteNote(it)
                }
            }
    }

    fun getChildNotes(parentId: Long): Flow<List<NoteDto>> {
        Timber.d("getChildNotes(${parentId}")
        return noteDao.getNotesWithCommitsForParentId(parentId).map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }
}