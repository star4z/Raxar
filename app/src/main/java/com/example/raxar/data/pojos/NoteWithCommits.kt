package com.example.raxar.data.pojos

import androidx.room.Embedded
import androidx.room.Relation
import com.example.raxar.data.models.Note
import com.example.raxar.data.models.NoteCommit

data class NoteWithCommits(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteId",
        entityColumn = "noteId"
    )
    val noteCommits: List<NoteCommit>
)