package com.example.todolistapp

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var taskList: ListView
    private val tasks = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val addButton = findViewById<Button>(R.id.button_add)
        val editText = findViewById<EditText>(R.id.editText_add_task)
        taskList = findViewById(R.id.taskList)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
        taskList.adapter = adapter

        loadTasks()

        addButton.setOnClickListener {
            addTask(editText)
        }
    }

     private fun addTask(editText: EditText) {
        val taskText = editText.text.toString()

        if (taskText.isNotEmpty()) {
            tasks.add(taskText)
            adapter.notifyDataSetChanged()
            saveTasks()
            editText.text.clear()
        }
    }

    private fun saveTasks() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("tasks", tasks.toSet())
        editor.apply()
    }

    private fun loadTasks() {
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val savedTasks = sharedPreferences.getStringSet("tasks", setOf()) ?: setOf()
        tasks.addAll(savedTasks)
        adapter.notifyDataSetChanged()
    }
}