package com.example.note.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.note.R
import com.example.note.dao.Notes
import kotlinx.android.synthetic.main.fragment_notes.view.*
import kotlinx.android.synthetic.main.item_notes.view.*

class NotesAdapter : ListAdapter<Notes, MyHolder>(Diff) {
    object Diff: DiffUtil.ItemCallback<Notes>() {
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val holder= LayoutInflater.from(parent.context).inflate(R.layout.item_notes,parent,false)
        return MyHolder(holder)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val notes=getItem(position)
        holder.itemView.smalltilte.text=notes.title
        holder.itemView.smallsubtilte.text=notes.subTitle
        holder.itemView.smalldatetime.text=notes.dateTime
        if(notes.color!=null){
            holder.itemView.cardview.setCardBackgroundColor(Color.parseColor(notes.color))
        }else{
            holder.itemView.cardview.setCardBackgroundColor(Color.parseColor("#171c26"))
        }
        if(notes.imgPath!=""){
            holder.itemView.smallimgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
            holder.itemView.smallimgNote.visibility=View.VISIBLE
        }else{
            holder.itemView.smallimgNote.visibility=View.GONE
        }
        if(notes.webLink!=""){
            holder.itemView.smalltextUri.text=notes.webLink
            holder.itemView.smalltextUri.visibility=View.VISIBLE
        }else{
            holder.itemView.smalltextUri.visibility=View.GONE
        }
        holder.itemView.setOnClickListener {
            Bundle().apply {
                putInt("noteId",notes.id!!)
                holder.itemView.findNavController().navigate(R.id.action_homeFragment_to_notesFragment,this)
            }
        }
    }
}

class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
}