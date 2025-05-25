package com.example.raxar.ui.commons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.raxar.data.NoteDto
import com.example.raxar.databinding.NoteListPreviewBinding

class NoteListPreviewAdapter(val itemCallback: (NoteDto) -> Unit) :
  ListAdapter<NoteDto, NoteListPreviewAdapter.ViewHolder>(NoteDiffCallback()) {

  inner class ViewHolder(private val binding: NoteListPreviewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindTo(noteDto: NoteDto) {
      binding.text.text = noteDto.title
      binding.root.setOnClickListener {
        itemCallback(noteDto)
      }
    }
  }

  override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int,
  ): ViewHolder {
    return ViewHolder(
      NoteListPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
  }

  override fun onBindViewHolder(
      holder: ViewHolder,
      position: Int,
  ) {
    holder.bindTo(getItem(position))
  }

  fun removeItem(viewHolder: ViewHolder): Pair<Int, NoteDto> {
    val list = mutableListOf(*currentList.toTypedArray())
    val adapterPosition = viewHolder.adapterPosition
    val noteDto = list.removeAt(adapterPosition)
    submitList(list)
    return Pair(adapterPosition, noteDto)
  }

  fun addItem(
      position: Int,
      noteDto: NoteDto,
  ) {
    val list = mutableListOf(*currentList.toTypedArray())
    list.add(position, noteDto)
    submitList(list)
  }

  public override fun getItem(position: Int): NoteDto {
    return super.getItem(position)
  }
}

private class NoteDiffCallback : DiffUtil.ItemCallback<NoteDto>() {

  override fun areItemsTheSame(
      oldItem: NoteDto,
      newItem: NoteDto,
  ) =
    oldItem.noteId == newItem.noteId

  override fun areContentsTheSame(
      oldItem: NoteDto,
      newItem: NoteDto,
  ) = oldItem == newItem
}