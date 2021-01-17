package com.example.raxar.ui.notedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.raxar.data.NoteDto
import com.example.raxar.databinding.NoteDetailFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.note_detail_fragment.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private val args: NoteDetailFragmentArgs by navArgs()
    private val viewModel: NoteDetailViewModel by viewModels()
    private var _binding: NoteDetailFragmentBinding? = null
    private val binding get() = _binding!!

    private var id: Long = 0L
    private var creating: Boolean = false

    companion object {
        const val ID_KEY = "id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val state = viewModel.getState()
        id = when {
            state.containsKey(ID_KEY) -> {
                state.getLong(ID_KEY)
            }
            args.noteId == 0L -> {
                creating = true
                viewModel.genId()
            }
            else -> {
                args.noteId
            }
        }
        state.putLong(ID_KEY, id)

        _binding = NoteDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.getNote(id, !creating)
            viewModel.note.removeObservers(viewLifecycleOwner)
            viewModel.note.observe(viewLifecycleOwner) {
                binding.title.setText(it.currentNoteCommit.title)
                binding.body.setText(it.currentNoteCommit.body)
            }
            binding.addChild.setOnClickListener {
                val note = saveNote()
                findNavController().navigate(
                    NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(parentNoteId = note.noteId)
                )
            }
        }

        val adapter = NoteListPreviewAdapter {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(it.noteId)
            )
        }
        children.adapter = adapter

        viewModel.childNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes.sortedByDescending { noteDto -> noteDto.currentNoteCommit.time })
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

    private fun saveNote(): NoteDto {
        val title = binding.title.text.toString()
        val body = binding.body.text.toString()
        creating = false
        return viewModel.saveNote(
            NoteDetailDto(title, body, args.parentNoteId)
        )
    }
}