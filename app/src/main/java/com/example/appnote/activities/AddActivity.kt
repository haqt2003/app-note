package com.example.appnote.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.appnote.R
import com.example.appnote.composables.formatDate
import com.example.appnote.databinding.ActivityAddBinding
import com.example.appnote.datas.NoteDAO
import com.example.appnote.datas.NoteDatabase
import com.example.appnote.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class AddActivity : AppCompatActivity() {

    private val binding: ActivityAddBinding by lazy {
        ActivityAddBinding.inflate(layoutInflater)
    }

    private lateinit var db: NoteDatabase
    private lateinit var noteDao: NoteDAO

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_add)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Room.databaseBuilder(this, NoteDatabase::class.java, "note-db").build()
        noteDao = db.noteDao()

        with(binding) {
            tvTime.text = formatDate(System.currentTimeMillis())

            btBack.setOnClickListener {
                finish()
            }

            btCheck.setOnClickListener {
                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                val editTime = System.currentTimeMillis()
                val note = Note(title = title, content = content, editTime = editTime)
                val intent = Intent()
                intent.putExtra("note", note)
                setResult(RESULT_OK, intent)
                lifecycleScope.launch(Dispatchers.IO) {
                    noteDao.insertNote(note)
                }
                finish()
            }
        }
    }
}