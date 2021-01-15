package com.example.raxar.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
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
        noteDao.deleteNote(noteWithCommits.note)
    }
}