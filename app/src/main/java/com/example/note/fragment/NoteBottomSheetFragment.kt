package com.example.note.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.note.R
import com.example.note.databinding.FragmentNotesBottonSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoteBottomSheetFragment : BottomSheetDialogFragment(
    R.layout.fragment_notes_botton_sheet
) {
    private var _binding: FragmentNotesBottonSheetBinding? = null
    private val binding get() = _binding!!
    private var selectedColor = "#171c26"

    private var noteId = -1

    companion object {
        fun newInstance(id: Int?): NoteBottomSheetFragment {
            val args = Bundle()
            val fragment = NoteBottomSheetFragment()
            fragment.arguments = args
            fragment.noteId = id ?: -1
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBottonSheetBinding.bind(view)
        if (noteId == -1) {
            binding.deleteNote.visibility = View.VISIBLE
        } else {
            binding.deleteNote.visibility = View.GONE
        }
        setListener()
    }

    private fun setListener() {
        binding.run {
            fnote1.setOnClickListener {
                imgNote1.setImageResource(R.drawable.ic_tick)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(0)
                selectedColor = "#4e33ff"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Blue")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote2.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(R.drawable.ic_tick)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(0)
                selectedColor = "#ffd633"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Yellow")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote3.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(R.drawable.ic_tick)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(0)
                selectedColor = "#ffffff"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "White")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote4.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(R.drawable.ic_tick)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(0)
                selectedColor = "#ae3b76"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Purple")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote5.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(R.drawable.ic_tick)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(0)
                selectedColor = "#0aebaf"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Green")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote6.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(R.drawable.ic_tick)
                imgNote7.setImageResource(0)
                selectedColor = "#ff7746"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Orange")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            fnote7.setOnClickListener {
                imgNote1.setImageResource(0)
                imgNote2.setImageResource(0)
                imgNote3.setImageResource(0)
                imgNote4.setImageResource(0)
                imgNote5.setImageResource(0)
                imgNote6.setImageResource(0)
                imgNote7.setImageResource(R.drawable.ic_tick)
                selectedColor = "#202734"
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Black")
                intent.putExtra("selectedColor", selectedColor)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            }
            addImg.setOnClickListener {
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Image")
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                dismiss()
            }
            addUri.setOnClickListener {
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "Uri")
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                dismiss()
            }
            deleteNote.setOnClickListener {
                val intent = Intent("bottom_sheet_action")
                intent.putExtra("action", "DeleteNote")
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                dismiss()
            }
        }

    }
}