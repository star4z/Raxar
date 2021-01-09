package com.example.raxar.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.raxar.R
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.note_list_fragment.*

@AndroidEntryPoint
class NotesFragment : Fragment() {

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.note_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NoteListPreviewAdapter {
            findNavController().navigate(
                NotesFragmentDirections.actionNoteListFragmentToNoteDetailFragment(it.noteId)
            )
        }
        recyclerview.adapter = adapter

        viewModel.notes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        add_note.setOnClickListener {
            val action = NotesFragmentDirections.actionNoteListFragmentToNoteDetailFragment(0L)
            findNavController().navigate(action)
        }
    }
}