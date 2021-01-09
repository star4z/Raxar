package com.example.raxar.ui.notepreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.raxar.R
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.note_detail_fragment.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private val args: NoteDetailFragmentArgs by navArgs()
    private val viewModel: NoteDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.note_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.noteId > 0L) {
            lifecycleScope.launch {
                viewModel.getNote(args.noteId)
                viewModel.note.observe(viewLifecycleOwner) {
                    title.setText(it.currentNoteCommit.title)
                    body.setText(it.currentNoteCommit.body)
                }
            }
        }

        children.adapter = NoteListPreviewAdapter {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(it.noteId)
            )
        }
    }
}