package com.example.appnote.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appnote.composables.formatDate
import com.example.appnote.databinding.LayoutItemBinding
import com.example.appnote.models.Note

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val listener: OnAdapterListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)

        holder.itemView.setOnClickListener {
            listener.onClick(note)
        }

        holder.itemView.setOnLongClickListener {
            listener.onLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun updateNotes(newNotes: MutableList<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    interface OnAdapterListener {
        fun onClick(note: Note)
        fun onLongClick(note: Note)
    }

    class NoteViewHolder(private val binding: LayoutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.tvTitle.text = note.title
            binding.tvContent.text = note.content
            binding.tvDate.text = formatDate(note.editTime)
        }
    }
}