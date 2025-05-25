package com.example.raxar.ui.notedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.raxar.R
import com.example.raxar.data.NoteDto
import com.example.raxar.databinding.NoteDetailFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewAdapter
import com.example.raxar.util.SwipeCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@AndroidEntryPoint
class NoteDetailFragment : Fragment() {

  private val viewModel: NoteDetailViewModel by viewModels()
  private var _binding: NoteDetailFragmentBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = NoteDetailFragmentBinding.inflate(inflater, container, false)
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
      runBlocking {
        val note = saveNote()
        findNavController().navigate(
          NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(
            it.noteId,
            parentNoteId = note.noteId
          )
        )
      }
    }
    binding.children.adapter = adapter

    addCallbackForUndoDeleteSnackbar(adapter)

    viewModel.note.removeObservers(viewLifecycleOwner)
    viewModel.note.observe(viewLifecycleOwner) { noteDto ->
      noteDto?.let {
        binding.title.setText(noteDto.title)
        binding.body.setText(noteDto.body)
        adapter.submitList(noteDto.childNotes.sortedByDescending { noteDto -> noteDto.time })
      }
    }
    binding.addChild.setOnClickListener {
      runBlocking {
        val note = saveNote()
        findNavController().navigate(
          NoteDetailFragmentDirections.actionNoteDetailFragmentSelf(parentNoteId = note.noteId)
        )
      }
    }

    binding.toolbar.setNavigationOnClickListener {
      findNavController().navigateUp()
    }
  }

  private fun addCallbackForUndoDeleteSnackbar(adapter: NoteListPreviewAdapter) {
    val swipeCallback = SwipeCallback(requireContext()) { viewHolder, _ ->
      val (position, noteDto) = adapter.removeItem(viewHolder as NoteListPreviewAdapter.ViewHolder)
      val snackbar =
        Snackbar.make(binding.root, getString(R.string.deleted_note), Snackbar.LENGTH_LONG)
      snackbar.setAction(getString(R.string.undo)) {
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
    itemTouchHelper.attachToRecyclerView(binding.children)
  }

  override fun onStop() {
    super.onStop()
    runBlocking { saveNote() }
  }

  private suspend fun saveNote(): NoteDto {
    return viewModel.saveNote(
      NoteDetailDto(binding.title.text.toString(), binding.body.text.toString())
    )
  }
}