package com.example.appnote.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.appnote.R
import com.example.appnote.composables.formatDate
import com.example.appnote.databinding.ActivityEditBinding
import com.example.appnote.datas.NoteDAO
import com.example.appnote.datas.NoteDatabase
import com.example.appnote.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditActivity : AppCompatActivity() {

    private val binding: ActivityEditBinding by lazy {
        ActivityEditBinding.inflate(layoutInflater)
    }

    private lateinit var db: NoteDatabase
    private lateinit var noteDao: NoteDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_edit)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Room.databaseBuilder(this, NoteDatabase::class.java, "note-db").build()
        noteDao = db.noteDao()

        val intent = intent
        val note = intent.getParcelableExtra<Note>("note")

        with(binding) {
            note?.let {
                etTitle.setText(it.title)
                etContent.setText(it.content)
                tvTime.text = formatDate(it.editTime)
            }

            btBack.setOnClickListener {
                finish()
            }

            btCheck.setOnClickListener {
                val title = etTitle.text.toString()
                val content = etContent.text.toString()
                lifecycleScope.launch(Dispatchers.IO) {
                    if (note != null) {
                        noteDao.updateNote(
                            Note(
                                note.id,
                                title,
                                content,
                                System.currentTimeMillis()
                            )
                        )
                    }
                }
                val newNote = note?.let { it1 ->
                    Note(
                        it1.id, title = title,
                        content = content,
                        editTime = System.currentTimeMillis()
                    )
                }
                val resultIntent = Intent()
                resultIntent.putExtra("note", newNote)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }

    }
}