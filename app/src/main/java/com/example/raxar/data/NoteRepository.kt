package com.example.raxar.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
                ?: error("Note ${noteWithCommits.note.noteId} contained no commits!"),
            noteCommits
        )
    }

    suspend fun saveNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        val noteRowId = noteDao.saveNote(noteWithCommits.note)
        noteDao.saveNoteCommits(noteWithCommits.noteCommits.map { noteCommit ->
            NoteCommit(
                noteCommit.noteCommitId,
                noteRowId,
                noteCommit.parentNoteCommitId,
                noteCommit.time,
                noteCommit.color,
                noteCommit.title,
                noteCommit.body
            )
        })
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
}