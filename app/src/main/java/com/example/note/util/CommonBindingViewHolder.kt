package com.example.note.util

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class CommonBindingViewHolder<VB: ViewBinding>(
    val binding: VB
): RecyclerView.ViewHolder(binding.root)