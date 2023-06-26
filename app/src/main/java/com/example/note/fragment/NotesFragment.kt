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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.example.note.R
import com.example.note.dao.NoteDao
import com.example.note.dao.Note
import com.example.note.databinding.FragmentNotesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {
    var currrent: String? = null
    var READ_STORAGE_PERM = 123
    var REQUSET_CODE_IMAGE = 123

    @Inject
    lateinit var dao: NoteDao
    var selectedColor = "#171c26"

    private var selectedImgPath = ""
    private var webLink = ""
    private var noteId: Int = -1

    private var _binding: FragmentNotesBinding? = null
    private val binding: FragmentNotesBinding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)
        noteId = arguments?.getInt("noteId") ?: -1
        if (noteId != -1) {
            lifecycleScope.launch {
                val notes = dao.getSpecificNote(noteId)
                binding.edNoteTitles.setText(notes.title)
                binding.edsubtitle.setText(notes.subTitle)
                binding.edNotesText.setText(notes.noteText)
                binding.colorview.setBackgroundColor(Color.parseColor(notes.color))
                if (notes.imgPath != "") {
                    selectedImgPath = notes.imgPath!!
                    binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                    binding.layoutImage.visibility = View.VISIBLE
                    binding.imgDelete.visibility = View.VISIBLE
                    binding.imgNote.visibility = View.VISIBLE
                } else {
                    binding.layoutImage.visibility = View.GONE
                    binding.imgDelete.visibility = View.GONE
                    binding.imgNote.visibility = View.GONE
                }
                if (notes.webLink != "") {
                    webLink = notes.webLink!!
                    binding.textUri.visibility = View.VISIBLE
                    binding.imgdeleteuri.visibility = View.VISIBLE
                    binding.textUri.text = notes.webLink
                    binding.lineUri.visibility = View.VISIBLE
                    binding.edNotesUri.setText(notes.webLink)
                    binding.imgdeleteeduri.visibility = View.VISIBLE
                } else {
                    binding.imgdeleteeduri.visibility = View.GONE
                    binding.textUri.visibility = View.GONE
                }
            }
        }

        //广播
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        val sdf = SimpleDateFormat("yyyy/M/dd hh:mm:ss")
        currrent = sdf.format(Date())
        binding.date.text = currrent
        binding.imgdone.setOnClickListener {
            if (noteId != null) {
                updateNote()
            } else {
                saveNote()
            }
        }
        binding.imgback.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_notesFragment_to_homeFragment)
        }
        binding.imgmore.setOnClickListener {
            NoteBottomSheetFragment.newInstance(noteId)
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
    }

    private fun deleteNote() {
        lifecycleScope.launch {
            dao.deleteSpecificNote(noteId)
            Navigation.findNavController(requireView())
                .navigate(R.id.action_notesFragment_to_homeFragment)
        }
    }

    private fun updateNote() {
        lifecycleScope.launch {
            val notes = dao.getSpecificNote(noteId)
            notes.title = binding.edNoteTitles.text.toString()
            notes.subTitle = binding.edsubtitle.text.toString()
            notes.noteText = binding.edNotesText.text.toString()
            notes.dateTime = currrent
            notes.color = selectedColor
            notes.imgPath = selectedImgPath
            notes.webLink = webLink
            dao.updateNote(notes)
            binding.edNoteTitles.setText("")
            binding.edNotesText.setText("")
            binding.edsubtitle.setText("")
            binding.layoutImage.visibility = View.GONE
            binding.imgNote.visibility = View.GONE
            binding.textUri.visibility = View.GONE
            Navigation.findNavController(requireView())
                .navigate(R.id.action_notesFragment_to_homeFragment)
        }
    }

    private fun saveNote() {
        if (binding.edNoteTitles.text.isNullOrEmpty()) {
            Toast.makeText(context, "标题为空", Toast.LENGTH_SHORT).show()
        } else if (binding.edsubtitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "副标题为空", Toast.LENGTH_SHORT).show()
        } else if (binding.edNotesText.text.isNullOrEmpty()) {
            Toast.makeText(context, "正文为空", Toast.LENGTH_SHORT).show()
        } else {
            lifecycleScope.launch {
                val notes = Note()
                notes.title = binding.edNoteTitles.text.toString()
                notes.subTitle = binding.edsubtitle.text.toString()
                notes.noteText = binding.edNotesText.text.toString()
                notes.dateTime = currrent
                notes.color = selectedColor
                notes.imgPath = selectedImgPath
                notes.webLink = webLink
                dao.insertNotes(notes)
                binding.edNoteTitles.setText("")
                binding.edNotesText.setText("")
                binding.edsubtitle.setText("")
                binding.layoutImage.visibility = View.GONE
                binding.imgNote.visibility = View.GONE
                binding.textUri.visibility = View.GONE
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_notesFragment_to_homeFragment)
            }
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionColor = intent?.getStringExtra("action")
            when (actionColor) {
                "Blue" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Yellow" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "White" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Purple" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Green" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Orange" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Black" -> {
                    selectedColor = intent.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }

                "Image" -> {
                    readStorgeTask()
                    binding.lineUri.visibility = View.GONE
                }

                "Uri" -> {
                    binding.lineUri.visibility = View.VISIBLE
                }

                "DeleteNote" -> {
                    deleteNote()
                }

                else -> {
                    binding.imgNote.visibility = View.GONE
                    binding.layoutImage.visibility = View.GONE
                    binding.lineUri.visibility = View.GONE
                    selectedColor = intent?.getStringExtra("selectedColor")!!
                    binding.colorview.setBackgroundColor(Color.parseColor(selectedColor))
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