package com.choco.coroutineandroom

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.choco.coroutineandroom.catatan.Note

class MainActivity : AppCompatActivity() {

    companion object {
        const val ADD_NOTE_REQUEST = 1
        const val EDIT_NOTE_REQUEST = 2
    }

    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAddNote.setOnClickListener {
            startActivityForResult(
                Intent(this, AddEditNoteActivity::class.java),
                ADD_NOTE_REQUEST
            )
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
        val adapter = NoteAdapter()
        recycler_view.adapter = adapter
        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel::class.java)
        noteViewModel.getAllNotes().observe(this, Observer<List<Note>> {
            adapter.submitList(it)
        })
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.adapterPosition))
                Toast.makeText(baseContext, "Catatan dihapus!", Toast.LENGTH_SHORT).show()
            }
        }
        ).attachToRecyclerView(recycler_view)
        adapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClick(note: Note) {
                val intent = Intent(baseContext, AddEditNoteActivity::class.java)
                intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.id)
                intent.putExtra(AddEditNoteActivity.EXTRA_JUDUL, note.title)
                intent.putExtra(AddEditNoteActivity.EXTRA_DESKRIPSI, note.description)
                12
                intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITAS, note.priority)
                startActivityForResult(intent, EDIT_NOTE_REQUEST)
            }
        })

        fun onCreateOptionsMenu(menu: Menu?): Boolean {
            menuInflater.inflate(R.menu.main_menu, menu)
            return true
        }

        fun onOptionsItemSelected(item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete_all_notes -> {
                    noteViewModel.deleteAllNotes()
                    Toast.makeText(this, "Semua sudah dihapus!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == ADD_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
                val newNote = data.getStringExtra(AddEditNoteActivity.EXTRA_DESKRIPSI)?.let {
                    data!!.getStringExtra(AddEditNoteActivity.EXTRA_JUDUL)?.let { it1 ->
                        Note(
                            it1,
                            it,
                            data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITAS, 1)
                        )
                    }
                }
                if (newNote != null) {
                    noteViewModel.insert(newNote)
                }
                Toast.makeText(this, "Catatan disimpan!", Toast.LENGTH_SHORT).show()
            } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == Activity.RESULT_OK) {
                val id = data?.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1)
                if (id == -1) {
                    Toast.makeText(this, "Pembaharuan gagal!", Toast.LENGTH_SHORT).show()
                }
                val updateNote = data!!.getStringExtra(AddEditNoteActivity.EXTRA_JUDUL)?.let {
                    data.getStringExtra(AddEditNoteActivity.EXTRA_DESKRIPSI)?.let { it1 ->
                        Note(
                            it,
                            it1,
                            data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITAS, 1)
                        )
                    }
                }
                if (updateNote != null) {
                    updateNote.id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1)
                }
                if (updateNote != null) {
                    noteViewModel.update(updateNote)
                }
            } else {
                Toast.makeText(this, "Catatan tidak disimpan!", Toast.LENGTH_SHORT).show()
            }
        }


    }
}

