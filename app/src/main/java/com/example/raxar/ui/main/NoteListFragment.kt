package com.example.raxar.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.raxar.R
import com.example.raxar.data.Note
import com.example.raxar.data.NoteCommit
import com.example.raxar.data.NoteDto
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import kotlinx.android.synthetic.main.note_list_fragment.*
import java.time.ZonedDateTime

class NoteListFragment : Fragment() {

    companion object {
        fun newInstance() = NoteListFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.note_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notes = listOf(
            NoteDto(
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
        )
        recyclerview.adapter = object : NoteListPreviewAdapter(notes) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                holder.itemView.setOnClickListener {
                    findNavController().navigate(NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(this.notes[position].noteId))
                }
            }
        }

        add_note.setOnClickListener { v ->
            val action = NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(0L)
            findNavController().navigate(action)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}