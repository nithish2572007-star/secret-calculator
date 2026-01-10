package com.example.calc

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SecretNotesActivity : AppCompatActivity() {

    private lateinit var notesList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var encryptionHelper: EncryptionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_notes)

        encryptionHelper = EncryptionHelper("notes_key_alias")

        val etInput = findViewById<EditText>(R.id.etNoteInput)
        val btnAdd = findViewById<Button>(R.id.btnAddNote)
        val listView = findViewById<ListView>(R.id.lvNotes)

        // Load existing notes from Storage
        notesList = loadNotes()

        // Setup the list adapter
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notesList)
        listView.adapter = adapter

        // Add Note Logic
        btnAdd.setOnClickListener {
            val text = etInput.text.toString()
            if (text.isNotEmpty()) {
                notesList.add(text)
                adapter.notifyDataSetChanged()
                saveNotes()
                etInput.text.clear()
            }
        }

        // Delete Note Logic (Long press to delete)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            notesList.removeAt(position)
            adapter.notifyDataSetChanged()
            saveNotes()
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
            true
        }
    }

    // Helper to Save notes (Persistence)
    private fun saveNotes() {
        val sharedPref = getSharedPreferences("SecretData", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val encryptedNotes = notesList.map { note ->
            val (iv, encryptedData) = encryptionHelper.encrypt(note)
            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT)
            "$ivString,$encryptedString"
        }

        val joinedNotes = encryptedNotes.joinToString("|||")
        editor.putString("NOTES_KEY", joinedNotes)
        editor.apply()
    }

    // Helper to Load notes
    private fun loadNotes(): ArrayList<String> {
        val sharedPref = getSharedPreferences("SecretData", Context.MODE_PRIVATE)
        val savedString = sharedPref.getString("NOTES_KEY", "") ?: ""

        if (savedString.isEmpty()) {
            return ArrayList()
        }

        val encryptedNotes = savedString.split("|||")
        val decryptedNotes = encryptedNotes.mapNotNull { encryptedNote ->
            try {
                val parts = encryptedNote.split(",")
                if (parts.size == 2) {
                    val iv = Base64.decode(parts[0], Base64.DEFAULT)
                    val encryptedData = Base64.decode(parts[1], Base64.DEFAULT)
                    encryptionHelper.decrypt(iv, encryptedData)
                } else {
                    null
                }
            } catch (e: Exception) {
                // Log error or handle corrupted data
                null
            }
        }
        return ArrayList(decryptedNotes)
    }
}