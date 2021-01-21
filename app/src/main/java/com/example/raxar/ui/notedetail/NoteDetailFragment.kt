package com.example.raxar.ui.notedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.raxar.data.NoteDto
import com.example.raxar.databinding.NoteDetailFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import com.example.raxar.util.SwipeCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

    private val args: NoteDetailFragmentArgs by navArgs()
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

        viewModel.note.removeObservers(viewLifecycleOwner)
        viewModel.note.observe(viewLifecycleOwner) {
            binding.title.setText(it.currentNoteCommit.title)
            binding.body.setText(it.currentNoteCommit.body)
        }
        binding.addChild.setOnClickListener {
            val note = saveNote()
            note?.let {
                findNavController().navigate(
                    NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(parentNoteId = note.noteId)
                )
            }
        }

        val adapter = NoteListPreviewAdapter {
            findNavController().navigate(
                NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(
                    it.noteId,
                    parentNoteId = viewModel.note.value!!.noteId
                )
            )
        }
        binding.children.adapter = adapter

        viewModel.childNotes.removeObservers(viewLifecycleOwner)
        viewModel.childNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes.sortedByDescending { noteDto -> noteDto.currentNoteCommit.time })
        }

        val swipeCallback: SwipeCallback = object : SwipeCallback(requireContext()) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
                    val removedNote = adapter.removeItem(viewHolder.adapterPosition)
                    viewModel.deleteNote(removedNote)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(binding.children)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        saveNote()
    }

    private fun saveNote(): NoteDto? {
        return if (!saved) {
            val title = binding.title.text.toString()
            val body = binding.body.text.toString()
            saved = true
            viewModel.saveNote(
                NoteDetailDto(title, body, args.parentNoteId)
            )
        } else {
            null
        }
    }
}