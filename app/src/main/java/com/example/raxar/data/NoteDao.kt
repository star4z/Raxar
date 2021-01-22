package com.example.raxar.data

import androidx.room.*
import com.example.raxar.data.models.Note
import com.example.raxar.data.models.NoteCommit
import com.example.raxar.data.pojos.NoteWithCommits
import com.example.raxar.data.pojos.NoteWithCurrentCommit
import com.example.raxar.data.pojos.NoteWithCurrentCommitAndChildNotes
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNote(note: Note)

    @Update
    abstract fun updateNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertNoteCommit(noteCommit: NoteCommit)

    @Delete
    abstract fun deleteNote(note: Note)

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNotesWithCommits(noteId: Long): Flow<NoteWithCommits?>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId=:parentNoteId")
    abstract fun getNotesWithCommitsForParentId(parentNoteId: Long): Flow<List<NoteWithCommits>>

    @Transaction
    @Query("SELECT * FROM notes WHERE parentNoteId IS NULL")
    abstract fun getNotesWithCommitsForRootNode(): Flow<List<NoteWithCommits>>

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

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNoteWithCurrentCommit(noteId: Long): Flow<NoteWithCurrentCommit?>

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId=:noteId LIMIT 1")
    abstract fun getNoteWithCurrentCommitAndChildNotes(noteId: Long): Flow<NoteWithCurrentCommitAndChildNotes>
}