package com.example.raxar.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @Transaction
    @Query("SELECT * FROM notes INNER JOIN note_commits ON notes.currentNoteCommitId = note_commits.noteCommitId WHERE notes.parentNoteId IS NULL")
    abstract fun getNotesWithCurrentCommitsForRootNode(): Flow<List<NoteWithCurrentCommitView>>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNoteWithCurrentCommitAndChildNotes(noteId: Long): Flow<NoteWithCurrentCommitAndChildNotes>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNote(note: Note)

    @Update
    abstract fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNoteCommit(noteCommit: NoteCommit)

    @Delete
    abstract fun deleteNote(note: Note)

    @Transaction
    open fun insertNoteAndNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        insertNote(note)
        insertNoteCommit(currentNoteCommit)
    }

    @Transaction
    open fun updateNoteAndInsertNoteCommit(note: Note, currentNoteCommit: NoteCommit) {
        updateNote(note)
        insertNoteCommit(currentNoteCommit)
    }

}