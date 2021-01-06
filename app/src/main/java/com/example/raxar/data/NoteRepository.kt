package com.example.raxar.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(private val noteDao: NoteDao) {

    fun getNotes(): Flow<List<NoteDto>> {
        return noteDao.getNotesWithCommits().map { list: List<NoteWithCommits> ->
            list.map { noteWithCommits ->
                noteWithCommitsToNoteDto(noteWithCommits)
            }
        }
    }

    private fun noteWithCommitsToNoteDto(noteWithCommits: NoteWithCommits): NoteDto {
        val noteCommits = noteWithCommits.noteCommits.map { it.noteCommitId to it }.toMap()
        return NoteDto(
            noteWithCommits.note.noteId,
            noteWithCommits.note.parentId,
            noteCommits[noteWithCommits.note.currentNoteCommitId] ?: error("Note ${noteWithCommits.note.noteId} contained no commits!"),
            noteCommits
        )
    }

    suspend fun saveNote(noteDto: NoteDto) {
        val noteWithCommits = noteDtoToNoteWithCommits(noteDto)
        noteDao.saveNote(noteWithCommits.note)
        noteDao.saveNoteCommit(*noteWithCommits.noteCommits.toTypedArray())
    }

    private fun noteDtoToNoteWithCommits(noteDto: NoteDto): NoteWithCommits {
        return NoteWithCommits(
            Note(
                noteDto.noteId,
                noteDto.parentId,
                noteDto.currentNoteCommit.noteCommitId
            ),
            noteDto.noteCommits.values.toList()
        )
    }
}