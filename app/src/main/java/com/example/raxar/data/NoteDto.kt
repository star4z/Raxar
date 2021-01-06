package com.example.raxar.data

data class NoteDto(
    val noteId: Long,
    val parentId: Long,
    val currentNoteCommit: NoteCommit,
    val noteCommits: Map<Long, NoteCommit> // Mapped by id
)