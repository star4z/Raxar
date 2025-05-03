package com.example.raxar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM notes INNER JOIN note_commits ON notes.currentNoteCommitId = note_commits.noteCommitId WHERE notes.parentNoteId IS NULL")
    abstract fun getNotesWithCurrentCommitsForRootNode(): Flow<List<NoteWithCurrentCommitView>>

    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNoteWithCurrentCommitAndChildNotes(noteId: Long): Flow<NoteWithCurrentCommitAndChildNotes?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNote(note: Note): Long

    @Update
    abstract fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNoteCommit(noteCommit: NoteCommit): Long

    @Delete
    abstract fun deleteNote(note: Note)

    open fun createNote(parentNoteId: Long?): Flow<NoteWithCurrentCommitAndChildNotes?> {
        val note = Note(parentNoteId = parentNoteId)
        val noteId = insertNote(note)
        val noteCommit = NoteCommit(noteId = noteId)
        val noteCommitId = insertNoteCommit(noteCommit)
        updateNote(note.copy(noteId = noteId, currentNoteCommitId = noteCommitId))
        return getNoteWithCurrentCommitAndChildNotes(noteId)
    }

    @Transaction
    open fun updateNoteAndInsertNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        val noteCommitId = insertNoteCommit(currentNoteCommit)
        updateNote(note.copy(currentNoteCommitId = noteCommitId))
    }

}