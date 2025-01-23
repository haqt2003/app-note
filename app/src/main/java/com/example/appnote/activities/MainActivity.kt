package com.example.appnote.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.appnote.R
import com.example.appnote.adapters.NoteAdapter
import com.example.appnote.databinding.ActivityMainBinding
import com.example.appnote.datas.NoteDAO
import com.example.appnote.datas.NoteDatabase
import com.example.appnote.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), NoteAdapter.OnAdapterListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: NoteAdapter
    private val notes = mutableListOf<Note>()

    private lateinit var db: NoteDatabase
    private lateinit var noteDao: NoteDAO

    private val addNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                data?.let {
                    val note = it.getParcelableExtra<Note>("note")
                    if (note != null) {
                        notes.add(0, note)
                        adapter.notifyItemInserted(0)
                        binding.rvNote.scrollToPosition(0)
                    }
                }
            }
        }

    private val editNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                data?.let {
                    val note = it.getParcelableExtra<Note>("note")
                    val noteEdit = notes.find { it.id == note?.id }
                    val position = notes.indexOfFirst { it.id == note?.id }
                    if (noteEdit != null) {
                        noteEdit.title = note?.title.toString()
                        noteEdit.content = note?.content.toString()
                        noteEdit.editTime = System.currentTimeMillis()
                        adapter.notifyItemChanged(position)
                    }
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Room.databaseBuilder(this, NoteDatabase::class.java, "note-db").build()
        noteDao = db.noteDao()

        adapter = NoteAdapter(notes, this)
        binding.rvNote.adapter = adapter
        binding.rvNote.layoutManager = LinearLayoutManager(this@MainActivity)

        lifecycleScope.launch(Dispatchers.IO) {
            val notesFromDB = noteDao.getAll()
            notes.addAll(notesFromDB)
            notes.sortByDescending { it.editTime }
            withContext(Dispatchers.Main) {
                adapter.notifyDataSetChanged()
            }
        }

        with(binding) {
            btAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, AddActivity::class.java)
                addNoteLauncher.launch(intent)
            }

            ivSort.setOnClickListener {
                clSort.visibility = View.VISIBLE
                vBlur.visibility = View.VISIBLE
            }

            tvCancel.setOnClickListener {
                clSort.visibility = View.GONE
                vBlur.visibility = View.GONE
            }

            tvOk.setOnClickListener {
                when (rgSort.checkedRadioButtonId) {
                    R.id.rb_newest -> notes.sortByDescending { it.editTime }
                    R.id.rb_oldest -> notes.sortBy { it.editTime }
                    R.id.rb_az -> notes.sortBy { it.title }
                    R.id.rb_za -> notes.sortByDescending { it.title }
                }
                adapter.notifyDataSetChanged()
                clSort.visibility = View.GONE
                vBlur.visibility = View.GONE
            }

            etSearch.addTextChangedListener {
                val searchQuery = binding.etSearch.text.toString()

                if (searchQuery.isEmpty()) {
                    adapter.updateNotes(notes)
                    tvOk.setOnClickListener {
                        when (rgSort.checkedRadioButtonId) {
                            R.id.rb_newest -> notes.sortByDescending { it.editTime }
                            R.id.rb_oldest -> notes.sortBy { it.editTime }
                            R.id.rb_az -> notes.sortBy { it.title }
                            R.id.rb_za -> notes.sortByDescending { it.title }
                        }
                        adapter.notifyDataSetChanged()
                        clSort.visibility = View.GONE
                        vBlur.visibility = View.GONE
                    }
                } else {
                    val tmpItems = mutableListOf<Note>()
                    tmpItems.addAll(notes)
                    val queryItem = tmpItems.filter {
                        it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(
                            searchQuery,
                            ignoreCase = true
                        )
                    }.toMutableList()
                    adapter.updateNotes(queryItem)
                    tvOk.setOnClickListener {
                        when (rgSort.checkedRadioButtonId) {
                            R.id.rb_newest -> queryItem.sortByDescending { it.editTime }
                            R.id.rb_oldest -> queryItem.sortBy { it.editTime }
                            R.id.rb_az -> queryItem.sortBy { it.title }
                            R.id.rb_za -> queryItem.sortByDescending { it.title }
                        }
                        adapter.notifyDataSetChanged()
                        clSort.visibility = View.GONE
                        vBlur.visibility = View.GONE
                    }
                }
            }

            vBlur.setOnClickListener {
                clSort.visibility = View.GONE
                vBlur.visibility = View.GONE
            }
        }
    }

    override fun onClick(note: Note) {
        val intent = Intent(this@MainActivity, EditActivity::class.java)
        intent.putExtra("note", note)
        editNoteLauncher.launch(intent)
    }

    override fun onLongClick(note: Note) {
        val position = notes.indexOfFirst { it.id == note.id }
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    noteDao.deleteNote(note)
                    notes.remove(note)
                    withContext(Dispatchers.Main) {
                        adapter.notifyItemRemoved(position)
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}