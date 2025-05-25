package com.example.raxar.ui.notelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.raxar.databinding.NoteListFragmentBinding
import com.example.raxar.ui.commons.NoteListPreviewGraphAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteListFragment : Fragment() {

  private val viewModel: NoteListViewModel by viewModels()
  private var _binding: NoteListFragmentBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
  ): View {
    _binding = NoteListFragmentBinding.inflate(inflater, container, false)
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

    val graphAdapter = NoteListPreviewGraphAdapter()
    binding.graph.adapter = graphAdapter

    viewModel.notes.removeObservers(viewLifecycleOwner)
    viewModel.notes.observe(viewLifecycleOwner) { notes ->
      graphAdapter.submitList(notes)
    }

    binding.addNote.setOnClickListener {
      val action = NoteListFragmentDirections.actionNoteListFragmentToNoteDetailFragment()
      findNavController().navigate(action)
    }

    binding.viewNotesList.setOnClickListener {
      val action =
        NoteListFragmentDirections.actionNoteListFragmentToNoteListRecyclerViewFragment()
      findNavController().navigate(action)
    }
  }
}