package com.example.raxar.data.dbviews

import androidx.room.DatabaseView
import java.time.ZonedDateTime

@DatabaseView(
    "SELECT notes.noteId, notes.parentNoteId, notes.currentNoteCommitId, " +
            "note_commits.parentNoteCommitId, note_commits.time, note_commits.color, " +
            "note_commits.title, note_commits.body " +
            "FROM notes INNER JOIN note_commits ON notes.currentNoteCommitId = note_commits.noteCommitId"
)
data class NoteWithCurrentCommitView(
    val noteId: Long,
    val parentNoteId: Long?,
    val currentNoteCommitId: Long,
    val parentNoteCommitId: Long,
    val time: ZonedDateTime,
    val color: String,
    val title: String,
    val body: String
)