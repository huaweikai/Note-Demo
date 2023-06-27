package com.example.note.fragment

import android.Manifest
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.note.vm.AddNoteViewModel
import com.example.note.vm.ChangeNoteEvent
import com.example.note.R
import com.example.note.databinding.FragmentNotesBinding
import com.example.note.onError
import com.example.note.onSuccess
import com.example.note.util.FileType
import com.example.note.util.copyToSandBox
import com.example.note.util.observe
import com.example.note.util.requestPermission
import com.example.note.util.setTextWithEnd
import com.example.note.util.viewbindingdelegate.viewBinding
import com.example.note.vm.BottomNoteEvent
import com.example.note.vm.NoteActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditNoteFragment : Fragment(R.layout.fragment_notes) {

    private val addNoteViewModel by viewModels<AddNoteViewModel>()

    private val activityViewModel by activityViewModels<NoteActivityViewModel>()

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent(), ::disposeChooseImage)

    private val binding: FragmentNotesBinding by viewBinding(FragmentNotesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        addNoteViewModel.noteId = arguments?.getInt("noteId") ?: -1
        binding.imgdone.setOnClickListener {
            addNoteViewModel.saveNote()
        }
        binding.imgback.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_notesFragment_to_homeFragment)
        }
        binding.ivMore.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("is_new_note", addNoteViewModel.noteId == -1)
            bundle.putString("color", addNoteViewModel.noteStateFlow.value.color)
            val fragmentBottomSheetFragment = NoteBottomSheetFragment()
            fragmentBottomSheetFragment.arguments = bundle
            fragmentBottomSheetFragment.show(
                parentFragmentManager,
                "NoteBottomSheetFragment"
            )
        }
        binding.btnLinkRight.setOnClickListener {
            addNoteViewModel.sendChangeNoteEvent(ChangeNoteEvent.ChangeNoteLink(
                binding.edNotesUri.text.toString()
            ))
        }
        binding.btnLinkCancel.setOnClickListener {
            binding.lineUri.isVisible = false
        }
        binding.textUri.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.edNotesUri.text.toString()))
            startActivity(intent)
        }
        binding.imgDelete.setOnClickListener {
            addNoteViewModel.sendChangeNoteEvent(ChangeNoteEvent.ChangeNoteImage(""))
        }
        binding.ivDeleteUrl.setOnClickListener {
            binding.lineUri.isVisible = false
            addNoteViewModel.sendChangeNoteEvent(ChangeNoteEvent.ChangeNoteLink(""))
        }
        binding.ivClearLink.setOnClickListener {
            binding.edNotesUri.setText("")
        }
        binding.edNoteTitles.addTextChangedListener {
            changeNoteContent()
        }
        binding.edNotesText.addTextChangedListener {
            changeNoteContent()
        }
        binding.edsubtitle.addTextChangedListener {
            changeNoteContent()
        }
        binding.edNotesUri.addTextChangedListener {
            binding.ivClearLink.isVisible = !binding.edNotesUri.text.isNullOrBlank()
        }
    }

    private fun changeNoteContent() {
        addNoteViewModel.sendChangeNoteEvent(
            ChangeNoteEvent.ChangeNoteContent(
                binding.edNoteTitles.text.toString(),
                binding.edsubtitle.text.toString(),
                binding.edNotesText.text.toString()
            )
        )
    }

    private fun initObserve() {
        addNoteViewModel.noteStateFlow.observe(
            viewLifecycleOwner
        ) { note ->
            binding.edNoteTitles.setTextWithEnd(note.title)
            binding.edsubtitle.setTextWithEnd(note.subTitle)
            binding.edNotesText.setTextWithEnd(note.noteText)
            binding.colorview.setBackgroundColor(Color.parseColor(note.color))
            val imageIsEmpty = note.imgPath.isNullOrBlank()
            binding.layoutImage.isVisible = !imageIsEmpty
            binding.imgDelete.isVisible = !imageIsEmpty
            binding.imgNote.isVisible = !imageIsEmpty
            binding.tvDate.text = note.dateTime
            if (!imageIsEmpty) {
                binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(note.imgPath))
            }
            binding.textUri.text = note.webLink
            val webLinkIsEmpty = note.webLink.isNullOrBlank()
            binding.textUri.isVisible = !webLinkIsEmpty
            binding.ivDeleteUrl.isVisible = !webLinkIsEmpty
            binding.lineUri.isVisible = false
            binding.edNotesUri.setText(note.webLink)
            binding.lineWeb.isVisible = !webLinkIsEmpty
        }

        addNoteViewModel.saveNoteChannel.observe(
            viewLifecycleOwner
        ) { findNavController().popBackStack() }

        activityViewModel.noteEventSharedFlow.observe(viewLifecycleOwner, ::disposeBottomEvent)

        addNoteViewModel.screenStatusChannel.observe(viewLifecycleOwner) { lce ->
            lce.onSuccess { message, _ ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }.onError {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disposeBottomEvent(event: BottomNoteEvent) {
        when (event) {
            is BottomNoteEvent.ChooseColorEvent -> {
                addNoteViewModel.sendChangeNoteEvent(
                    ChangeNoteEvent.ChangeNoteColor(
                        event.color
                    )
                )
            }
            is BottomNoteEvent.ChooseImageEvent-> {
                binding.lineUri.isVisible = false
                requireActivity().requestPermission(
                    listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                ) {
                    launcher.launch("image/*")
                }
            }
            is BottomNoteEvent.ChooseWebLinkEvent -> {
                binding.lineUri.isVisible = true
            }
            is BottomNoteEvent.DeleteNoteEvent -> {
                addNoteViewModel.deleteNote()
            }
        }
    }

    private fun disposeChooseImage(uri: Uri?) {
        if (uri == null) return
        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
            val filePath = requireContext().copyToSandBox(FileType.Cache, uri)
            addNoteViewModel.sendChangeNoteEvent(ChangeNoteEvent.ChangeNoteImage(filePath))
        }
    }
}