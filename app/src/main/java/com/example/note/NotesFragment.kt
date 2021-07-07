package com.example.note

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.example.note.dao.NoteDao
import com.example.note.dao.Notes
import com.example.note.database.NotesDatabase
import com.example.note.util.NoteBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_notes.*
import kotlinx.android.synthetic.main.fragment_notes.imgmore
import kotlinx.android.synthetic.main.fragment_notes_botton_sheet.*
import kotlinx.android.synthetic.main.item_notes.view.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class NotesFragment : Fragment(),EasyPermissions.PermissionCallbacks,EasyPermissions.RationaleCallbacks{
    var currrent:String?=null
    var READ_STORAGE_PERM=123
    var REQUSET_CODE_IMAGE =123
    lateinit var dao: NoteDao
    var selectedColor="#171c26"

    private var selectedImgPath=""
    private var webLink=""
    var noteid:Int?=-1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val notesDatabase= NotesDatabase.getDataBase(requireContext())
        dao=notesDatabase.noteDao()
        noteid= arguments?.getInt("noteId")
        Log.d("TAG", "onViewCreated: ${noteid}")
        val noteBottomSheetFragment=NoteBottomSheetFragment.newInstance(noteid)
        if(noteid!=null){
            lifecycleScope.launch {
                val notes=dao.getSpecificNote(noteid!!)
                edNoteTitles.setText(notes.title)
                edsubtitle.setText(notes.subTitle)
                edNotesText.setText(notes.noteText)
                colorview.setBackgroundColor(Color.parseColor(notes.color))
                if(notes.imgPath!=""){
                    selectedImgPath= notes.imgPath!!
                    imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                    layoutImage.visibility=View.VISIBLE
                    imgDelete.visibility=View.VISIBLE
                    imgNote.visibility=View.VISIBLE
                }else{
                    layoutImage.visibility=View.GONE
                    imgDelete.visibility=View.GONE
                    imgNote.visibility=View.GONE
                }
                if(notes.webLink!=""){
                    webLink=notes.webLink!!
                    textUri.visibility=View.VISIBLE
                    imgdeleteuri.visibility=View.VISIBLE
                    textUri.text=notes.webLink
                    lineUri.visibility=View.VISIBLE
                    edNotesUri.setText(notes.webLink)
                    imgdeleteeduri.visibility=View.VISIBLE
                }else{
                    imgdeleteeduri.visibility=View.GONE
                    textUri.visibility=View.GONE
                }
            }
        }

        //广播
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        val sdf= SimpleDateFormat("yyyy/M/dd hh:mm:ss")
        currrent=sdf.format(Date())
        date.text=currrent
        imgdone.setOnClickListener {
            if(noteid!=null){
                updateNote()
            }else{
                saveNote()
            }
        }
        imgback.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_notesFragment_to_homeFragment)
        }
        imgmore.setOnClickListener {
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager,"Note Bottom")
        }
        btnright.setOnClickListener {
            if(edNotesUri.text.toString().trim().isNotEmpty()){
                checkWebUri()
            }else{
                Toast.makeText(requireContext(),"链接是空的",Toast.LENGTH_SHORT).show()
            }
        }
        btncancel.setOnClickListener {
            lineUri.visibility=View.GONE
        }
        textUri.setOnClickListener {
            val intent=Intent(Intent.ACTION_VIEW,Uri.parse(edNotesUri.text.toString()))
            startActivity(intent)
        }
        imgDelete.setOnClickListener {
            selectedImgPath=""
            layoutImage.visibility=View.GONE
        }
        imgdeleteuri.setOnClickListener {
            lineUri.visibility=View.GONE
            webLink=""
        }
        imgdeleteeduri.setOnClickListener {
            webLink=""
            imgdeleteeduri.visibility=View.GONE
            lineUri.visibility=View.GONE
            textUri.visibility=View.GONE
        }
    }
    private fun deleteNote(){
        lifecycleScope.launch {
            dao.deleteSpecificNote(noteid!!)
            Navigation.findNavController(requireView())
                .navigate(R.id.action_notesFragment_to_homeFragment)
        }
    }
    private fun updateNote(){
        lifecycleScope.launch {
            val notes = dao.getSpecificNote(noteid!!)
            notes.title = edNoteTitles.text.toString()
            notes.subTitle = edsubtitle.text.toString()
            notes.noteText = edNotesText.text.toString()
            notes.dateTime = currrent
            notes.color = selectedColor
            notes.imgPath = selectedImgPath
            notes.webLink = webLink
            dao.updateNote(notes)
            edNoteTitles.setText("")
            edNotesText.setText("")
            edsubtitle.setText("")
            layoutImage.visibility=View.GONE
            imgNote.visibility = View.GONE
            textUri.visibility = View.GONE
            Navigation.findNavController(requireView())
                .navigate(R.id.action_notesFragment_to_homeFragment)
        }
    }
    private fun saveNote(){
        if(edNoteTitles.text.isNullOrEmpty()){
            Toast.makeText(context,"标题为空", Toast.LENGTH_SHORT).show()
        }
        else if(edsubtitle.text.isNullOrEmpty()){
            Toast.makeText(context,"副标题为空", Toast.LENGTH_SHORT).show()
        }
        else if(edNotesText.text.isNullOrEmpty()){
            Toast.makeText(context,"正文为空", Toast.LENGTH_SHORT).show()
        }
        else {
            lifecycleScope.launch {
                val notes = Notes()
                notes.title = edNoteTitles.text.toString()
                notes.subTitle = edsubtitle.text.toString()
                notes.noteText = edNotesText.text.toString()
                notes.dateTime = currrent
                notes.color = selectedColor
                notes.imgPath = selectedImgPath
                notes.webLink = webLink
                dao.insertNotes(notes)
                edNoteTitles.setText("")
                edNotesText.setText("")
                edsubtitle.setText("")
                layoutImage.visibility=View.GONE
                imgNote.visibility = View.GONE
                textUri.visibility = View.GONE
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_notesFragment_to_homeFragment)
            }
        }
    }
    private val broadcastReceiver:BroadcastReceiver=object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val actionColor=intent?.getStringExtra("action")
            when(actionColor){
                "Blue"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Yellow"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "White"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Purple"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Green"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Orange"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Black"->{
                    selectedColor= intent.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Image"->{
                    readStorgeTask()
                    lineUri.visibility=View.GONE
                }
                "Uri"->{
                    lineUri.visibility=View.VISIBLE
                }
                "DeleteNote"->{
                    deleteNote()
                }
                else->{
                    imgNote.visibility=View.GONE
                    layoutImage.visibility=View.GONE
                    lineUri.visibility=View.GONE
                    selectedColor= intent?.getStringExtra("selectedColor")!!
                    colorview.setBackgroundColor(Color.parseColor(selectedColor))
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    //链接
    private fun checkWebUri(){
        if(Patterns.WEB_URL.matcher(edNotesUri.text.toString()).matches()){
            lineUri.visibility=View.GONE
            edNotesUri.isEnabled=false
            webLink=edNotesUri.text.toString()
            lineWeb.visibility=View.VISIBLE
            textUri.visibility=View.VISIBLE
            textUri.text=webLink
        }else{
            Toast.makeText(requireContext(),"链接是错误的",Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasReadStorgePerm():Boolean{
        return EasyPermissions.hasPermissions(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    private fun readStorgeTask(){
        if(hasReadStorgePerm()){
            pickImageGallery()
        }else{

            EasyPermissions.requestPermissions(
                requireActivity(),
                "此app需要读取权限",
                READ_STORAGE_PERM,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImageGallery() {
        val intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if(intent.resolveActivity(requireActivity().packageManager)!=null){
            startActivityForResult(intent,REQUSET_CODE_IMAGE)
        }
    }
    private fun getPathFromUri(containeruri:Uri):String?{
        val fiflePath:String?
        val cursor=requireActivity().contentResolver.query(containeruri,null,
        null,null,null)
        Log.d("TAG", "getPathFromUri: $containeruri")
        if(cursor==null){
            fiflePath=containeruri.path
        }else{
            cursor.moveToFirst()
            val index=cursor.getColumnIndex("_data")
            fiflePath=cursor.getString(index)
            cursor.close()
        }
        return fiflePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUSET_CODE_IMAGE&&resultCode==RESULT_OK){
            if(data!=null){
                val selectedImageUri=data.data
                if(selectedImageUri!=null){
                    try {
                        val inputStream=requireActivity().contentResolver.openInputStream(selectedImageUri)
                        val bitmap=BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility=View.VISIBLE
                        layoutImage.visibility=View.VISIBLE
                        selectedImgPath=getPathFromUri(selectedImageUri)!!
                    }catch (e:Exception){
                        Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
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

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,requireActivity())
    }
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
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