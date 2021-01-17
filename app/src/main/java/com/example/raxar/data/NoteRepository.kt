package com.example.raxar.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    fun getNotes(): Flow<List<NoteDto>> {
        return noteDao.getNotesWithCommits().map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }

    private fun noteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits): NoteDto {
        val noteCommits = noteWithCommits.noteCommits
        return NoteDto(
            noteWithCommits.note.noteId,
            noteWithCommits.note.parentId,
            noteCommits.find { noteCommit -> noteCommit.noteCommitId == noteWithCommits.note.currentNoteCommitId }
            //This should never happen but exists here to fail gracefully in the case of a bug
                ?: NoteCommit(
                    0L,
                    0L,
                    0L,
                    ZonedDateTime.now(),
                    "",
                    "",
                    ""
                ),
            noteCommits
        )
    }

    fun saveNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        GlobalScope.launch {
            noteDao.saveNote(noteWithCommits.note)
            noteDao.saveNoteCommit(noteDto.currentNoteCommit)
        }
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
        return noteDao.getNotesWithCommitsForParentId(parentId).map {
            it.map(this::noteWithCommitsToNoteDto)
        }
    }
}