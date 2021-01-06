package com.example.raxar.ui.notepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.raxar.R
import com.example.raxar.data.NoteCommit
import com.example.raxar.data.NoteDto
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import kotlinx.android.synthetic.main.note_detail_fragment.*
import java.time.ZonedDateTime

class NoteDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.note_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val note = NoteDto(
            1L,
            0L,
            NoteCommit(
                1L,
                1L,
                0L,
                ZonedDateTime.now(),
                "",
                "Title",
                "body"
            ),
            mapOf(
                1L to NoteCommit(
                    1L,
                    1L,
                    0L,
                    ZonedDateTime.now(),
                    "",
                    "Title",
                    "body"
                )
            )
        )

        title.setText(note.currentNoteCommit.title)
        body.setText(note.currentNoteCommit.body)

        val childNotes = listOf(
            NoteDto(
                2L,
                1L,
                NoteCommit(
                    2L,
                    2L,
                    0L,
                    ZonedDateTime.now(),
                    "",
                    "Title",
                    "body"
                ),
                mapOf(
                    2L to NoteCommit(
                        2L,
                        2L,
                        0L,
                        ZonedDateTime.now(),
                        "",
                        "Title",
                        "body"
                    )
                )
            )
        )

        children.adapter = object : NoteListPreviewAdapter(childNotes) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                holder.itemView.setOnClickListener {
                    findNavController().navigate(
                        NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(notes[position].noteId))
                }
            }
        }
    }
}