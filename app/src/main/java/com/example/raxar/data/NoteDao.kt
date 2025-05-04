package com.example.raxar.data

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT * FROM notes INNER JOIN note_commits ON notes.currentNoteCommitId = note_commits.noteCommitId WHERE notes.parentNoteId IS NULL")
    abstract fun getNotesWithCurrentCommitsForRootNode(): Flow<List<NoteWithCurrentCommitView>>

    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNoteWithCurrentCommitAndChildNotes(noteId: Long): LiveData<NoteWithCurrentCommitAndChildNotes?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNote(note: Note): Long

    @Update
    abstract fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNoteCommit(noteCommit: NoteCommit): Long

    @Delete
    abstract fun deleteNote(note: Note)

    @Transaction
    open fun createNote(note: Note, noteCommit: NoteCommit): NoteWithCurrentCommitAndChildNotes {
        // Insert new note.
        val noteId = insertNote(note)

        // Insert new commit.
        val commitWithNoteId = noteCommit.copy(noteId = noteId)
        val noteCommitId = insertNoteCommit(commitWithNoteId)

        // Update the note to point to the current commit.
        val noteWithCommit = note.copy(noteId = noteId, currentNoteCommitId = noteCommitId)
        updateNote(noteWithCommit)

        // Return the new note.
        return NoteWithCurrentCommitAndChildNotes(noteWithCommit, commitWithNoteId)
    }

    @Query("SELECT * FROM note_commits WHERE noteCommitId=:noteCommitId LIMIT 1")
    abstract fun getNoteCommit(noteCommitId: Long): NoteCommit

    @Update
    abstract fun updateNoteCommit(noteCommit: NoteCommit)

    @Transaction
    open fun updateNoteAndInsertNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        // Insert new commit.
        // Note new commits should not have a parent ID.
        val noteCommitId = insertNoteCommit(currentNoteCommit)

        // Update the previous commit to point to the current one.
        val previousCommit = getNoteCommit(note.currentNoteCommitId)
        updateNoteCommit(previousCommit.copy(parentNoteCommitId = noteCommitId))

        // Update the note to point to the current commit.
        updateNote(note.copy(currentNoteCommitId = noteCommitId))
    }

}