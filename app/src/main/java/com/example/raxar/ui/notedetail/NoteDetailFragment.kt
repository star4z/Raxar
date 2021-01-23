package com.example.raxar.ui.notedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.raxar.data.NoteDto
import com.example.raxar.databinding.NoteDetailFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import com.example.raxar.util.SwipeCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private val viewModel: NoteDetailViewModel by viewModels()
    private var _binding: NoteDetailFragmentBinding? = null
    private val binding get() = _binding!!

    private var saved = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        saved = false

        _binding = NoteDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = NoteListPreviewAdapter {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(
                    it.noteId,
                    parentNoteId = viewModel.note.value!!.noteId
                )
            )
        }
        binding.children.adapter = adapter

        val swipeCallback = SwipeCallback(requireContext()) { viewHolder, _ ->
            val removedNote = adapter.getItem(viewHolder.adapterPosition)
            viewModel.deleteNote(removedNote)
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.children)

        viewModel.note.removeObservers(viewLifecycleOwner)
        viewModel.note.observe(viewLifecycleOwner) { noteDto ->
            noteDto?.let {
                binding.title.setText(noteDto.title)
                binding.body.setText(noteDto.body)
                adapter.submitList(noteDto.childNotes.sortedByDescending { noteDto -> noteDto.time })
            }
        }
        binding.addChild.setOnClickListener {
            val note = saveNote()
            note?.let {
                findNavController().navigate(
                    NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(parentNoteId = note.noteId)
                )
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        when {
            isNewAndEmpty() -> {
                Snackbar.make(
                    requireActivity().window.decorView.rootView,
                    "Discarded empty note.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            hasBeenModified() -> {
                saveNote()
            }
        }
    }

    private fun hasBeenModified() = viewModel.needToCreate ||
            viewModel.note.value?.let {
                binding.title.text.toString() != it.title ||
                        binding.body.text.toString() != it.body
            } ?: false

    private fun isNewAndEmpty() = viewModel.needToCreate &&
            binding.title.text.toString().isEmpty() &&
            binding.body.text.toString().isEmpty()

    private fun saveNote(): NoteDto? {
        return if (!saved) {
            val title = binding.title.text.toString()
            val body = binding.body.text.toString()
            saved = true
            viewModel.saveNote(
                NoteDetailDto(title, body)
            )
        } else {
            null
        }
    }
}