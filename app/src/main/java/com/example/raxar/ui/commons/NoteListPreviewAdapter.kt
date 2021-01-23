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

    class ViewHolder(val noteListPreviewBinding: NoteListPreviewBinding) :
        RecyclerView.ViewHolder(noteListPreviewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NoteListPreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.noteListPreviewBinding.text.text = item.title
        holder.itemView.setOnClickListener {
            itemCallback(item)
        }
    }

    fun removeItem(adapterPosition: Int): NoteDto {
        val list = mutableListOf(*currentList.toTypedArray())
        val noteDto = list.removeAt(adapterPosition)
        submitList(list)
        notifyItemRemoved(adapterPosition)
        return noteDto
    }

    public override fun getItem(position: Int): NoteDto {
        return super.getItem(position)
    }
}

private class NoteDiffCallback : DiffUtil.ItemCallback<NoteDto>() {

    override fun areItemsTheSame(oldItem: NoteDto, newItem: NoteDto) =
        oldItem.noteId == newItem.noteId

    override fun areContentsTheSame(oldItem: NoteDto, newItem: NoteDto) = oldItem == newItem

}