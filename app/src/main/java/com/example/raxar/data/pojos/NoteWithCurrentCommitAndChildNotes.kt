package com.example.raxar.data.pojos

import androidx.room.Embedded
import androidx.room.Relation
import com.example.raxar.data.models.Note
import com.example.raxar.data.models.NoteCommit

data class NoteWithCurrentCommitAndChildNotes(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "currentNoteCommitId",
        entityColumn = "noteCommitId"
    )
    val noteCommit: NoteCommit,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "parentNoteId"
    )
    val childNotes: List<Note>
)