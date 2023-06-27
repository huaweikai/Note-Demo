package com.example.note.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import com.example.note.databinding.LayoutColorItemBinding
import com.example.note.util.CommonBindingViewHolder
import com.example.note.util.diffUtil
import com.example.note.util.getCircleDrawable

class ChooseColorAdapter : ListAdapter<String, CommonBindingViewHolder<LayoutColorItemBinding>>(
    String::class.java.diffUtil
) {

    var currentColor: String? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommonBindingViewHolder<LayoutColorItemBinding> {
        val holder = CommonBindingViewHolder(
            LayoutColorItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        holder.itemView.setOnClickListener {
            val colorString = currentList[holder.adapterPosition]
            currentColor = colorString
            listener?.invoke(colorString)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: CommonBindingViewHolder<LayoutColorItemBinding>,
        position: Int
    ) {
        val color = currentList[position].toColorInt()
        holder.binding.viewColor.background = holder.itemView.context.getCircleDrawable(color)
        holder.binding.ivTick.isVisible = currentColor == currentList[position]
    }

    private var listener: ((color: String) -> Unit)? = null

    fun setColorListener(listener: (color: String) -> Unit) {
        this.listener = listener
    }
}