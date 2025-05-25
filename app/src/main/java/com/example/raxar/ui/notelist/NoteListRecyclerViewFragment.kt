package com.example.raxar.ui.notelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.raxar.R
import com.example.raxar.databinding.NoteListFragment2Binding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import com.example.raxar.util.SwipeCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NoteListRecyclerViewFragment : Fragment() {
  private val viewModel: NoteListViewModel by viewModels()
  private var _binding: NoteListFragment2Binding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = NoteListFragment2Binding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)

    val adapter = NoteListPreviewAdapter {
      findNavController().navigate(
        NoteListRecyclerViewFragmentDirections.actionNoteListRecyclerViewFragmentToNoteDetailFragment(
          it.noteId
        )
      )
    }
    binding.recyclerview.adapter = adapter

    val swipeCallback = SwipeCallback(requireContext()) { viewHolder, _ ->
      val (position, noteDto) = adapter.removeItem(viewHolder as NoteListPreviewAdapter.ViewHolder)
      val snackbar = Snackbar.make(binding.root, R.string.deleted_note, Snackbar.LENGTH_LONG)
      snackbar.setAction(R.string.undo) {
        Timber.d("Undo pressed.")
        adapter.addItem(position, noteDto)
      }.addCallback(object : Snackbar.Callback() {
        override fun onDismissed(
          transientBottomBar: Snackbar?,
          event: Int,
        ) {
          if (event != DISMISS_EVENT_ACTION) {
            Timber.d("Snackbar was not dismissed by touch event.")
            viewModel.deleteNote(noteDto)
          }
        }
      }).show()
    }

    val itemTouchHelper = ItemTouchHelper(swipeCallback)
    itemTouchHelper.attachToRecyclerView(binding.recyclerview)

    viewModel.notes.removeObservers(viewLifecycleOwner)
    viewModel.notes.observe(viewLifecycleOwner) { notes ->
      adapter.submitList(notes.sortedByDescending { it.time })
    }

    binding.addNote.setOnClickListener {
      val action =
        NoteListRecyclerViewFragmentDirections.actionNoteListRecyclerViewFragmentToNoteDetailFragment()
      findNavController().navigate(action)
    }

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }
}