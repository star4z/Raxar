package com.example.raxar.data

import androidx.room.Embedded
import androidx.room.Relation

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
  val childNotes: List<NoteWithCurrentCommitView> = listOf(),
)