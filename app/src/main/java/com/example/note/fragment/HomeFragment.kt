package com.example.note.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.note.vm.NoteViewModel
import com.example.note.R
import com.example.note.adapter.NotesAdapter
import com.example.note.databinding.FragmentHomeBinding
import com.example.note.util.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {

    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)

    val noteViewModel by activityViewModels<NoteViewModel>()

    private val notesAdapter by lazy {
        NotesAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addNotes.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_notesFragment)
        }
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = notesAdapter
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val search = newText?.trim()
                noteViewModel.searchDatabase(search)
                return true
            }

        })
        binding.ivSettings.setOnClickListener {
//            startActivity(Intent(requireContext(), SettingsActivity::class.java))
            val path = preferences.getString("back_path", null)
            if (path == null) {
                selectBackupPath.launch(null)
            } else {
                noteViewModel.backupToFile(path)
            }
        }
        binding.ivRestore.setOnClickListener {
            val path = preferences.getString("back_path", null)
            if (path == null) {
                selectBackupPath.launch(null)
            } else {
                noteViewModel.restore(path)
            }
        }
        initObserver()
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                noteViewModel.noteDataList.collect {
                    notesAdapter.submitList(it)
                }
            }
        }
    }

    private val selectBackupPath = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            val modeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(uri, modeFlags)
            preferences.edit {
                putString("back_path", uri.toString())
            }
            noteViewModel.backupToFile(uri.toString())
        }
    }

    private val preferences get() = PreferenceManager.getDefaultSharedPreferences(requireContext())
}