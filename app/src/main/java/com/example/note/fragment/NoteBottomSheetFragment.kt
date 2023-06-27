package com.example.note.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.example.note.R
import com.example.note.adapter.ChooseColorAdapter
import com.example.note.databinding.FragmentNotesBottonSheetBinding
import com.example.note.util.viewbindingdelegate.viewBinding
import com.example.note.vm.BottomNoteEvent
import com.example.note.vm.NoteActivityViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoteBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_notes_botton_sheet) {
    private val binding by viewBinding(FragmentNotesBottonSheetBinding::bind)

    private val activityViewModel by activityViewModels<NoteActivityViewModel>()

    companion object {
        val colorList = listOf(
            "#4e33ff", "#ffd633", "#808080", "#ae3b76", "#0aebaf", "#ff7746", "#202734"
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteNote.isVisible = arguments?.getBoolean("is_new_note") == false
        colorAdapter.currentColor = arguments?.getString("color") ?: colorList[0]
        setListener()
    }

    private val colorAdapter by lazy {
        val adapter = ChooseColorAdapter()
        adapter.submitList(colorList)
        adapter
    }

    private fun setListener() {
        binding.rvColor.adapter = colorAdapter
        colorAdapter.setColorListener {
            activityViewModel.sendEvent(BottomNoteEvent.ChooseColorEvent(it))
        }
        binding.deleteNote.setOnClickListener {
            activityViewModel.sendEvent(BottomNoteEvent.DeleteNoteEvent)
        }
        binding.addImg.setOnClickListener {
            activityViewModel.sendEvent(BottomNoteEvent.ChooseImageEvent)
            closeBottomSheet()
        }
        binding.addUri.setOnClickListener {
            activityViewModel.sendEvent(BottomNoteEvent.ChooseWebLinkEvent)
            closeBottomSheet()
        }
    }

    private fun closeBottomSheet() {
        parentFragmentManager.commit {
            remove(this@NoteBottomSheetFragment)
        }
    }
}