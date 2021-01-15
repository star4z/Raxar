package com.example.raxar.ui.commons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.raxar.R
import com.example.raxar.data.NoteDto
import kotlinx.android.synthetic.main.note_list_preview.view.*

class NoteListPreviewAdapter(val itemCallback: (NoteDto) -> Unit) :
    ListAdapter<NoteDto, NoteListPreviewAdapter.ViewHolder>(NoteDiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.note_list_preview, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.text.text = item.currentNoteCommit.title
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
}

private class NoteDiffCallback : DiffUtil.ItemCallback<NoteDto>() {

    override fun areItemsTheSame(oldItem: NoteDto, newItem: NoteDto) =
        oldItem.noteId == newItem.noteId

    override fun areContentsTheSame(oldItem: NoteDto, newItem: NoteDto) = oldItem == newItem

}