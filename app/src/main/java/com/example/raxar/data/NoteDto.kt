package com.example.raxar.data

import java.time.ZonedDateTime

data class NoteDto(
    val noteId: Long = 0,
    val parentNoteId: Long? = null,
    val noteCommitId: Long = 0,
    val time: ZonedDateTime = ZonedDateTime.now(),
    val color: String = "",
    val title: String = "",
    val body: String = "",
    val childNotes: List<NoteDto> = listOf(),
    val source: Source = Source.DATABASE
) {
    enum class Source {
        DATABASE, MEMORY
    }
}