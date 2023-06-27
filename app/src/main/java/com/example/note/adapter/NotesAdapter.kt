package com.example.note.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.note.R
import com.example.note.bean.Note
import com.example.note.databinding.ItemNotesBinding

class NotesAdapter : ListAdapter<Note, MyHolder>(Diff) {
    object Diff: DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val holder= LayoutInflater.from(parent.context).inflate(R.layout.item_notes,parent,false)
        return MyHolder(ItemNotesBinding.bind(holder))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val notes=getItem(position)
        holder.binding.smalltilte.text=notes.title
        holder.binding.smallsubtilte.text=notes.subTitle
        holder.binding.smalldatetime.text=notes.dateTime
        Log.e("TAG", "onBindViewHolder: ${notes.updateTime}", )
        holder.binding.cardview.setCardBackgroundColor(Color.parseColor(notes.color))
        if(notes.imgPath!=""){
            holder.binding.smallimgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
            holder.binding.smallimgNote.visibility=View.VISIBLE
        }else{
            holder.binding.smallimgNote.visibility=View.GONE
        }
        if(notes.webLink!=""){
            holder.binding.smalltextUri.text=notes.webLink
            holder.binding.smalltextUri.visibility=View.VISIBLE
        }else{
            holder.binding.smalltextUri.visibility=View.GONE
        }
        holder.itemView.setOnClickListener {
            Bundle().apply {
                putInt("noteId",notes.id)
                holder.itemView.findNavController().navigate(R.id.action_homeFragment_to_notesFragment,this)
            }
        }
    }
}

class MyHolder(
    val binding: ItemNotesBinding
) : RecyclerView.ViewHolder(binding.root) {
}