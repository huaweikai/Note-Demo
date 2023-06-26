package com.example.note.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.note.AddNoteViewModel
import com.example.note.ChangeNoteEvent
import com.example.note.R
import com.example.note.databinding.FragmentNotesBinding
import com.example.note.onError
import com.example.note.onSuccess
import com.example.note.util.setTextWithEnd
import com.example.note.util.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    var READ_STORAGE_PERM = 123
    var REQUSET_CODE_IMAGE = 123

    private val addNoteViewModel by viewModels<AddNoteViewModel>()

    private var selectedImgPath = ""
    private var webLink = ""

    private val binding: FragmentNotesBinding by viewBinding(FragmentNotesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        addNoteViewModel.noteId = arguments?.getInt("noteId") ?: -1

        //广播
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        binding.imgdone.setOnClickListener {
            addNoteViewModel.saveNote(
                binding.edNoteTitles.text.toString(),
                binding.edsubtitle.text.toString(),
                binding.edNotesText.text.toString(),
                selectedImgPath,
                webLink
            )
        }
        binding.imgback.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_notesFragment_to_homeFragment)
        }
        binding.imgmore.setOnClickListener {
            NoteBottomSheetFragment.newInstance(addNoteViewModel.noteId)
                .show(requireActivity().supportFragmentManager, "Note Bottom")
        }
        binding.btnright.setOnClickListener {
            if (binding.edNotesUri.text.toString().trim().isNotEmpty()) {
                checkWebUri()
            } else {
                Toast.makeText(requireContext(), "链接是空的", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btncancel.setOnClickListener {
            binding.lineUri.visibility = View.GONE
        }
        binding.textUri.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(binding.edNotesUri.text.toString()))
            startActivity(intent)
        }
        binding.imgDelete.setOnClickListener {
            selectedImgPath = ""
            binding.layoutImage.visibility = View.GONE
        }
        binding.imgdeleteuri.setOnClickListener {
            binding.lineUri.visibility = View.GONE
            webLink = ""
        }
        binding.imgdeleteeduri.setOnClickListener {
            webLink = ""
            binding.imgdeleteeduri.visibility = View.GONE
            binding.lineUri.visibility = View.GONE
            binding.textUri.visibility = View.GONE
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
        viewLifecycleOwner.lifecycleScope.launch {
            addNoteViewModel.noteStateFlow.collect { note ->
                binding.edNoteTitles.setTextWithEnd(note.title)
                binding.edsubtitle.setTextWithEnd(note.subTitle)
                binding.edNotesText.setTextWithEnd(note.noteText)
                binding.colorview.setBackgroundColor(Color.parseColor(note.color))
                val imageIsEmpty = note.imgPath.isNullOrBlank()
                binding.layoutImage.isVisible = !imageIsEmpty
                binding.imgDelete.isVisible = !imageIsEmpty
                binding.imgNote.isVisible = !imageIsEmpty
                if (!imageIsEmpty) {
                    binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(note.imgPath))
                }
                binding.textUri.text = note.webLink
                val webLinkIsEmpty = note.webLink.isNullOrBlank()
                binding.textUri.isVisible = !webLinkIsEmpty
                binding.imgdeleteuri.isVisible = !webLinkIsEmpty
                binding.lineUri.isVisible = !webLinkIsEmpty
                binding.imgdeleteeduri.isVisible = !webLinkIsEmpty
                binding.edNotesUri.setText(note.webLink)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addNoteViewModel.saveNoteChannel.collect { lce ->
                    lce.onSuccess { message, _ ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }.onError {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra("action")) {
                "Image" -> {
                    readStorgeTask()
                    binding.lineUri.visibility = View.GONE
                }
                "Uri" -> {
                    binding.lineUri.visibility = View.VISIBLE
                }

                "DeleteNote" -> {
                    addNoteViewModel.deleteNote()
                }

                else -> {
                    binding.imgNote.visibility = View.GONE
                    binding.layoutImage.visibility = View.GONE
                    binding.lineUri.visibility = View.GONE
                    val color = intent?.getStringExtra("selectedColor")!!
                    addNoteViewModel.sendChangeNoteEvent(ChangeNoteEvent.ChangeNoteColor(
                        color
                    ))
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    //链接
    private fun checkWebUri() {
        if (Patterns.WEB_URL.matcher(binding.edNotesUri.text.toString()).matches()) {
            binding.lineUri.visibility = View.GONE
            binding.edNotesUri.isEnabled = false
            webLink = binding.edNotesUri.text.toString()
            binding.lineWeb.visibility = View.VISIBLE
            binding.textUri.visibility = View.VISIBLE
            binding.textUri.text = webLink
        } else {
            Toast.makeText(requireContext(), "链接是错误的", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasReadStorgePerm(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun readStorgeTask() {
        if (hasReadStorgePerm()) {
            pickImageGallery()
        } else {

            EasyPermissions.requestPermissions(
                requireActivity(),
                "此app需要读取权限",
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(intent, REQUSET_CODE_IMAGE)
        }
    }

    private fun getPathFromUri(containeruri: Uri): String? {
        val fiflePath: String?
        val cursor = requireActivity().contentResolver.query(
            containeruri, null,
            null, null, null
        )
        Log.d("TAG", "getPathFromUri: $containeruri")
        if (cursor == null) {
            fiflePath = containeruri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex("_data")
            fiflePath = cursor.getString(index)
            cursor.close()
        }
        return fiflePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUSET_CODE_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                val selectedImageUri = data.data
                if (selectedImageUri != null) {
                    try {
                        val inputStream =
                            requireActivity().contentResolver.openInputStream(selectedImageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imgNote.setImageBitmap(bitmap)
                        binding.imgNote.visibility = View.VISIBLE
                        binding.layoutImage.visibility = View.VISIBLE
                        selectedImgPath = getPathFromUri(selectedImageUri)!!
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            requireActivity()
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(requireActivity()).build().show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRationaleAccepted(requestCode: Int) {
    }

    override fun onRationaleDenied(requestCode: Int) {

    }
}