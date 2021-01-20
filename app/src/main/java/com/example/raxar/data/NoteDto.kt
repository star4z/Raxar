package com.example.raxar.data

data class NoteDto(
    val noteId: Long = 0,
    val parentNoteId: Long = 0,
    val currentNoteCommit: NoteCommit = NoteCommit(),
    val noteCommits: List<NoteCommit> = listOf()
)