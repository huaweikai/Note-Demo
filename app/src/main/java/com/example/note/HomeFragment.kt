package com.example.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.note.adapter.NotesAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    val myViewmodel by activityViewModels<MyViewmodel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addNotes.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_notesFragment)
        }
        myViewmodel.updateHomedatalist()
        val notesadapter=NotesAdapter()
        recyclerView.apply {
            layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
            adapter=notesadapter
        }
        myViewmodel.homedatalist.observe(requireActivity(),{
            notesadapter.submitList(it)
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val search=newText?.trim()
                myViewmodel.homedatalist.removeObservers(requireActivity())
                myViewmodel.searchNotedatalist("%$search%")
                myViewmodel.homedatalist.observe(requireActivity(),{
                    notesadapter.submitList(it)
                })
                return true
            }

        })
    }
}