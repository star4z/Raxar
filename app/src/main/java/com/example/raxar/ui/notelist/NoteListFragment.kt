package com.example.raxar.ui.notelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.raxar.databinding.NoteListFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import com.example.raxar.util.SwipeCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.note_list_fragment.*


@AndroidEntryPoint
class NoteListFragment : Fragment() {

    private val viewModel: NoteListViewModel by viewModels()
    private var _binding: NoteListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NoteListFragmentBinding.inflate(inflater, container, false)
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
                NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment(it.noteId)
            )
        }
        binding.recyclerview.adapter = adapter

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
        itemTouchHelper.attachToRecyclerView(binding.recyclerview)

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes.sortedByDescending { it.currentNoteCommit.time })
        }

        add_note.setOnClickListener {
            val action = NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment()
            findNavController().navigate(action)
        }
    }
}